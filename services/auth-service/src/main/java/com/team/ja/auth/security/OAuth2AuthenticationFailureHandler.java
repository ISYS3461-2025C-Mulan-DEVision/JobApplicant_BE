package com.team.ja.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OAuth2 failure handler:
 * - Logs the error
 * - Redirects the user back to the frontend with error details
 *
 * Query parameters sent to frontend:
 * - status=failure
 * - error=<errorCode>
 * - errorDescription=<short message>
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

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
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {

        String errorCode = exception != null
            ? exception.getClass().getSimpleName()
            : "OAUTH2_AUTHENTICATION_FAILURE";

        String message = exception != null && StringUtils.hasText(exception.getMessage())
            ? sanitize(exception.getMessage())
            : "OAuth2 authentication failed";

        // Optional hint to choose a specific redirect in the allowed list
        String redirectHint = request.getParameter("redirect_uri");

        String targetUrl = buildFailureUrl(errorCode, message, redirectHint);

        log.warn("OAuth2 authentication failed: {} - {}. Redirecting to {}", errorCode, message, targetUrl);
        response.sendRedirect(targetUrl);
    }

    private String buildFailureUrl(String errorCode, String message, String redirectHint) {
        List<String> allowed = parseAuthorizedRedirectUris();
        String base = resolveRedirectBase(redirectHint, allowed);

        return UriComponentsBuilder.fromUriString(base)
            .queryParam("status", "failure")
            .queryParam("error", errorCode)
            .queryParam("errorDescription", truncate(message, 300))
            .build(true) // enable encoding
            .toUriString();
    }

    private List<String> parseAuthorizedRedirectUris() {
        if (!StringUtils.hasText(authorizedRedirectUrisCsv)) {
            List<String> single = new ArrayList<>();
            if (StringUtils.hasText(defaultRedirectUri)) {
                single.add(defaultRedirectUri.trim());
            }
            return single;
        }
        String[] parts = authorizedRedirectUrisCsv.split(",");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                result.add(p.trim());
            }
        }
        if (result.isEmpty() && StringUtils.hasText(defaultRedirectUri)) {
            result.add(defaultRedirectUri.trim());
        }
        return result;
    }

    private String resolveRedirectBase(String redirectHint, List<String> allowed) {
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
                // Ignore malformed hint and fallback
            }
            log.warn("Provided redirect hint '{}' is not in allowed list. Using fallback {}", redirectHint, fallback);
        }

        // Prefer first allowed, else fallback
        if (!allowed.isEmpty()) {
            return allowed.get(0);
        }
        return fallback;
    }

    private boolean isSameHostAndPath(URI a, URI b) {
        if (a == null || b == null) return false;

        boolean schemeEqual = Objects.equals(
            (a.getScheme() == null ? "http" : a.getScheme().toLowerCase()),
            (b.getScheme() == null ? "http" : b.getScheme().toLowerCase())
        );

        boolean hostEqual = Objects.equals(a.getHost(), b.getHost());

        int portA = (a.getPort() == -1 ? defaultPortForScheme(a.getScheme()) : a.getPort());
        int portB = (b.getPort() == -1 ? defaultPortForScheme(b.getScheme()) : b.getPort());
        boolean portEqual = portA == portB;

        boolean pathEqual = Objects.equals(normalizePath(a.getPath()), normalizePath(b.getPath()));

        return schemeEqual && hostEqual && portEqual && pathEqual;
    }

    private int defaultPortForScheme(String scheme) {
        if (scheme == null) return 80;
        return switch (scheme.toLowerCase()) {
            case "https" -> 443;
            default -> 80;
        };
    }

    private String normalizePath(String p) {
        if (!StringUtils.hasText(p)) return "/";
        if (p.endsWith("/") && p.length() > 1) {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    private String truncate(String s, int max) {
        if (!StringUtils.hasText(s)) return s;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String sanitize(String s) {
        // Basic sanitization to avoid leaking stack traces or reserved characters
        String cleaned = s.replaceAll("[\\r\\n\\t]+", " ").trim();
        // Remove potential quotes to keep URL clean
        cleaned = cleaned.replace("\"", "'");
        return cleaned;
    }
}
