package com.team.ja.gateway.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway info and health endpoints.
 * Uses reactive types (Mono) for Spring Cloud Gateway compatibility.
 */
@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    private final RouteLocator routeLocator;

    @Value("${spring.application.name}")
    private String applicationName;

    public GatewayController(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        response.put("message", "API Gateway is running");
        return Mono.just(response);
    }

    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("version", "0.0.1");
        response.put("description", "API Gateway - Entry point for all requests");
        return Mono.just(response);
    }

    @GetMapping("/routes")
    public Mono<Map<String, Object>> routes() {
        return routeLocator.getRoutes()
                .map(route -> Map.of(
                        "id", route.getId(),
                        "uri", route.getUri().toString()))
                .collectList()
                .map(routes -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("routes", routes);
                    response.put("count", routes.size());
                    return response;
                });
    }
}
