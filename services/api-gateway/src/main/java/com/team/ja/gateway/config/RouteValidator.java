package com.team.ja.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Route validator to determine which routes require authentication.
 */
@Component
public class RouteValidator {

    /**
     * List of public endpoints that don't require authentication.
     */
    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            // Auth endpoints
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/health",
            "/api/v1/auth/activate",
            
            // Actuator endpoints
            "/actuator",
            "/actuator/health",
            "/actuator/info",
            
            // Swagger/OpenAPI endpoints
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            
            // Gateway info
            "/gateway/health",
            "/gateway/info"
    );

    /**
     * Predicate to check if a request is for a secured endpoint.
     */
    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        return PUBLIC_ENDPOINTS.stream()
                .noneMatch(publicPath -> path.startsWith(publicPath) || path.contains(publicPath));
    };
}

