package com.team.ja.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

/**
 * A stateless, cookie-based AuthorizationRequestRepository implementation.
 *
 * Stores the OAuth2AuthorizationRequest in an HttpOnly cookie so we don't rely on HTTP sessions.
 * Also persists an optional "redirect_uri" parameter in a separate cookie to allow redirect selection
 * after successful authentication.
 *
 * Security notes:
 * - Cookies are marked HttpOnly and Path "/".
 * - The success/failure handlers are responsible for validating the redirect_uri against a whitelist.
 */
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME =
        "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // short-lived

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(
        HttpServletRequest request
    ) {
        return getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
            .map(cookie ->
                deserialize(cookie.getValue(), OAuth2AuthorizationRequest.class)
            )
            .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        // Persist the authorization request
        String serialized = serialize(authorizationRequest);
        addCookie(
            response,
            OAUTH2_AUTH_REQUEST_COOKIE_NAME,
            serialized,
            COOKIE_EXPIRE_SECONDS
        );

        // Persist the redirect_uri (optional) so we can honor user/SPA preference after login
        String redirectUriAfterLogin = request.getParameter(
            REDIRECT_URI_PARAM_COOKIE_NAME
        );
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            addCookie(
                response,
                REDIRECT_URI_PARAM_COOKIE_NAME,
                redirectUriAfterLogin,
                COOKIE_EXPIRE_SECONDS
            );
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(
            request
        );
        removeAuthorizationRequestCookies(request, response);
        return authRequest;
    }

    /**
     * Remove both the authorization request and redirect uri cookies.
     */
    public void removeAuthorizationRequestCookies(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    // ============================ Cookie Helpers ============================

    private Optional<Cookie> getCookie(
        HttpServletRequest request,
        String name
    ) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie != null && name.equals(cookie.getName())) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }

    private void addCookie(
        HttpServletResponse response,
        String name,
        String value,
        int maxAgeSeconds
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private void deleteCookie(
        HttpServletRequest request,
        HttpServletResponse response,
        String name
    ) {
        getCookie(request, name).ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
    }

    // ============================ Serialization Helpers ============================

    private String serialize(Object obj) {
        try (
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos)
        ) {
            out.writeObject(obj);
            return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to serialize object for cookie storage",
                e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(String encoded, Class<T> cls) {
        if (!StringUtils.hasText(encoded)) {
            return null;
        }
        byte[] data = Base64.getUrlDecoder().decode(encoded);
        try (
            ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data)
            )
        ) {
            Object obj = ois.readObject();
            if (obj == null) return null;
            if (!cls.isInstance(obj)) {
                throw new IllegalStateException(
                    "Deserialized object is not of expected type: " +
                        cls.getName()
                );
            }
            return (T) obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(
                "Failed to deserialize cookie value",
                e
            );
        }
    }
}
