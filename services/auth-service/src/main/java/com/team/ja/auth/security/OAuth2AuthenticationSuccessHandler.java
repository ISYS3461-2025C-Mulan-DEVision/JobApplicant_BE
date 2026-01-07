package com.team.ja.auth.security;

import com.team.ja.auth.kafka.UserRegisteredProducer;
import com.team.ja.auth.model.AuthCredential;
import com.team.ja.auth.repository.AuthCredentialRepository;
import com.team.ja.common.enumeration.AuthProvider;
import com.team.ja.common.enumeration.Role;
import com.team.ja.common.event.UserRegisteredEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 success handler:
 * - Upserts user into AuthCredential (Google provider)
 * - Issues JWT access and refresh tokens
 * - Redirects to frontend callback with tokens as query parameters
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final AuthCredentialRepository authCredentialRepository;

    private final JwtService jwtService;
    private final UserRegisteredProducer userRegisteredProducer;

    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    /**
     * Comma-separated list of whitelisted redirect URIs.
     * Example: http://localhost:3000/auth/callback,http://localhost:3000/login
     */
    @Value("${app.oauth2.authorized-redirect-uris:http://localhost:3000/auth/callback}")
    private String authorizedRedirectUrisCsv;

    /**
     * Default redirect URI if none provided/found.
     */
    @Value("${app.oauth2.default-redirect-uri:http://localhost:3000/auth/callback}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                log.warn(
                        "Authentication is not OAuth2AuthenticationToken. Falling back to default redirect.");
                response.sendRedirect(buildFailureUrl("INVALID_AUTH_TYPE"));
                return;
            }

            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            // Extract normalized Google attributes
            ProviderUserInfo userInfo = extractUserInfo(
                    oauth2User,
                    registrationId);

            // Upsert user
            AuthCredential credential = upsertUser(userInfo);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(
                    credential,
                    credential.getId(),
                    credential.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(credential);

            // Resolve optional redirect hint from request param
            String redirectHint = request.getParameter("redirect_uri");

            // Build redirect URI
            String targetUrl = buildTargetUrl(
                    credential,
                    accessToken,
                    refreshToken,
                    redirectHint);

            log.info(
                    "OAuth2 success for {}, redirecting to {}",
                    userInfo.getEmail(),
                    targetUrl);
            response.sendRedirect(targetUrl);
        } catch (com.team.ja.common.exception.ConflictException e) {
            log.warn("OAuth2 conflict: {}", e.getMessage());
            String targetUrl = buildFailureUrl("ACCOUNT_ALREADY_EXISTS", e.getMessage());
            response.sendRedirect(targetUrl);
        } catch (Exception ex) {
            log.error(
                    "OAuth2 authentication success handling failed: {}",
                    ex.getMessage(),
                    ex);
            String targetUrl = buildFailureUrl("OAUTH2_HANDLER_ERROR");
            response.sendRedirect(targetUrl);
        }
    }

    private AuthCredential upsertUser(ProviderUserInfo info) {
        Optional<AuthCredential> existingOpt = authCredentialRepository.findByEmail(info.getEmail());
        AuthCredential credential;

        if (existingOpt.isEmpty()) {
            // Create new OAuth2 user
            credential = AuthCredential.builder()
                    .email(info.getEmail())
                    .passwordHash(
                            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(
                                    UUID.randomUUID().toString()))
                    .role(Role.FREE)
                    .authProvider(AuthProvider.GOOGLE)
                    .providerId(info.getProviderId())
                    .emailVerified(info.isEmailVerified())
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            credential = authCredentialRepository.save(credential);
            // Align userId with primary key so downstream services share the same UUID
            credential.setUserId(credential.getId());
            credential = authCredentialRepository.save(credential);

            // Publish Kafka event for user-service to create user profile
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(credential.getId())
                    .email(info.getEmail())
                    .firstName(info.getGivenName())
                    .lastName(info.getFamilyName())
                    .countryAbbreviation(info.getCountry())
                    .build();
            userRegisteredProducer.sendUserRegisteredEvent(event);

            log.info(
                    "Created new OAuth2 user: {} (provider: GOOGLE)",
                    info.getEmail());
        } else {
            // Update existing user (link to Google if not already)
            credential = existingOpt.get();

            // Check for provider mismatch - Do not allow overtaking LOCAL accounts or other
            // providers
            if (credential.getAuthProvider() != AuthProvider.GOOGLE) {
                throw new com.team.ja.common.exception.ConflictException(
                        "Account exists with provider " + credential.getAuthProvider()
                                + ". Please login with that provider.");
            }

            credential.setLastLoginAt(LocalDateTime.now());
            credential.setEmailVerified(
                    credential.isEmailVerified() || info.isEmailVerified());

            if (StringUtils.hasText(info.getProviderId())) {
                credential.setProviderId(info.getProviderId());
            }
            // Ensure userId is aligned
            if (credential.getUserId() == null) {
                credential.setUserId(credential.getId());
            }

            credential = authCredentialRepository.save(credential);
            log.info(
                    "Updated existing user for OAuth2 login: {}",
                    info.getEmail());
        }

        return credential;
    }

    private String buildTargetUrl(
            AuthCredential credential,
            String accessToken,
            String refreshToken,
            String redirectHint) {
        List<String> allowed = parseAuthorizedRedirectUris();
        String base = resolveRedirectBase(redirectHint, allowed);

        return UriComponentsBuilder.fromUriString(base)
                .queryParam("status", "success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("expiresIn", accessTokenExpiration / 1000) // seconds
                .queryParam("userId", credential.getId())
                .queryParam("email", credential.getEmail())
                .queryParam("role", credential.getRole().name())
                .build()
                .toUriString();
    }

    private String buildFailureUrl(String errorCode) {
        return buildFailureUrl(errorCode, null);
    }

    private String buildFailureUrl(String errorCode, String description) {
        List<String> allowed = parseAuthorizedRedirectUris();
        String base = resolveRedirectBase(null, allowed);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(base)
                .queryParam("status", "failure")
                .queryParam("error", errorCode);

        if (StringUtils.hasText(description)) {
            builder.queryParam("errorDescription", description);
        }

        return builder.build().toUriString();
    }

    private String resolveRedirectBase(
            String redirectHint,
            List<String> allowed) {
        String fallback = StringUtils.hasText(defaultRedirectUri)
                ? defaultRedirectUri.trim()
                : "http://localhost:3000/auth/callback";

        if (StringUtils.hasText(redirectHint)) {
            try {
                URI hint = URI.create(redirectHint);
                for (String candidate : allowed) {
                    if (isSameHostAndPath(hint, URI.create(candidate))) {
                        return candidate;
                    }
                }
            } catch (Exception ignored) {
                // ignore parsing errors and use fallback
            }
            log.warn(
                    "Provided redirect hint '{}' is not in allowed list. Using fallback {}",
                    redirectHint,
                    fallback);
        }

        // If no hint or invalid, try to use the first allowed entry
        if (!allowed.isEmpty()) {
            return allowed.get(0);
        }
        return fallback;
    }

    private boolean isSameHostAndPath(URI a, URI b) {
        if (a == null || b == null)
            return false;
        boolean hostEqual = Objects.equals(a.getHost(), b.getHost());
        // Normalize default ports (e.g., -1, 80, 443)
        int portA = (a.getPort() == -1
                ? defaultPortForScheme(a.getScheme())
                : a.getPort());
        int portB = (b.getPort() == -1
                ? defaultPortForScheme(b.getScheme())
                : b.getPort());
        boolean portEqual = portA == portB;

        // Compare path ignoring trailing slash
        String pathA = normalizePath(a.getPath());
        String pathB = normalizePath(b.getPath());
        boolean pathEqual = Objects.equals(pathA, pathB);

        boolean schemeEqual = Objects.equals(
                (a.getScheme() == null ? "http" : a.getScheme().toLowerCase()),
                (b.getScheme() == null ? "http" : b.getScheme().toLowerCase()));

        return schemeEqual && hostEqual && portEqual && pathEqual;
    }

    private int defaultPortForScheme(String scheme) {
        if (scheme == null)
            return 80;
        return switch (scheme.toLowerCase()) {
            case "https" -> 443;
            default -> 80;
        };
    }

    private String normalizePath(String p) {
        if (!StringUtils.hasText(p))
            return "/";
        if (p.endsWith("/") && p.length() > 1) {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    private List<String> parseAuthorizedRedirectUris() {
        if (!StringUtils.hasText(authorizedRedirectUrisCsv)) {
            return Collections.singletonList(defaultRedirectUri);
        }
        String[] parts = authorizedRedirectUrisCsv.split(",");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                result.add(p.trim());
            }
        }
        if (result.isEmpty()) {
            result.add(defaultRedirectUri);
        }
        return result;
    }

    private ProviderUserInfo extractUserInfo(
            OAuth2User oauth2User,
            String registrationId) {
        Map<String, Object> attrs = oauth2User.getAttributes();
        String email = getString(attrs, "email");
        boolean emailVerified = getBoolean(attrs, "email_verified");

        // Google OIDC 'sub' is the stable identifier
        String providerId = getString(attrs, "sub");

        // Names
        String givenName = getString(attrs, "given_name");
        String familyName = getString(attrs, "family_name");

        // Country from Locale
        String country = null;
        String locale = getString(attrs, "locale");
        if (StringUtils.hasText(locale)) {
            // Examples: "en-US", "en_US"
            if (locale.contains("-")) {
                String[] parts = locale.split("-");
                if (parts.length > 1)
                    country = parts[1].toUpperCase();
            } else if (locale.contains("_")) {
                String[] parts = locale.split("_");
                if (parts.length > 1)
                    country = parts[1].toUpperCase();
            }
        }

        if (oauth2User instanceof OidcUser oidc) {
            if (!StringUtils.hasText(email)) {
                email = oidc.getEmail();
            }
            if (!emailVerified) {
                emailVerified = oidc.getEmailVerified() != null && oidc.getEmailVerified();
            }
            if (!StringUtils.hasText(givenName)) {
                givenName = oidc.getGivenName();
            }
            if (!StringUtils.hasText(familyName)) {
                familyName = oidc.getFamilyName();
            }
        }

        // Fall back to "name"
        if (!StringUtils.hasText(givenName) || !StringUtils.hasText(familyName)) {
            String fullName = getString(attrs, "name");
            if (StringUtils.hasText(fullName)) {
                String[] parts = fullName.trim().split("\\s+");
                if (parts.length >= 1 && !StringUtils.hasText(givenName)) {
                    givenName = parts[0];
                }
                if (parts.length >= 2 && !StringUtils.hasText(familyName)) {
                    familyName = parts[parts.length - 1];
                }
            }
        }

        // Safety defaults
        if (!StringUtils.hasText(givenName))
            givenName = "";
        if (!StringUtils.hasText(familyName))
            familyName = "";

        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException(
                    "Email not provided by OAuth2 provider for registrationId=" +
                            registrationId);
        }

        return new ProviderUserInfo(
                email,
                emailVerified,
                providerId,
                givenName,
                familyName,
                country);
    }

    private String getString(Map<String, Object> attrs, String key) {
        Object v = attrs.get(key);
        return v != null ? String.valueOf(v) : null;
    }

    private boolean getBoolean(Map<String, Object> attrs, String key) {
        Object v = attrs.get(key);
        if (v instanceof Boolean b)
            return b;
        if (v instanceof String s)
            return "true".equalsIgnoreCase(s);
        return false;
    }

    @Getter
    @AllArgsConstructor
    private static class ProviderUserInfo {

        private final String email;
        private final boolean emailVerified;
        private final String providerId;
        private final String givenName;
        private final String familyName;
        private final String country;
    }
}
