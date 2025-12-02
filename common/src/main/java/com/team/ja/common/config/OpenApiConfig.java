package com.team.ja.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * 
 * Access Swagger UI at: http://localhost:{port}/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:{port}/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title(formatServiceName(applicationName) + " API")
                .description("REST API documentation for " + formatServiceName(applicationName))
                .version("1.0.0")
                .contact(new Contact()
                        .name("Team JA")
                        .email("team@jobapplicant.com"))
                .license(new License()
                        .name("Private")
                        .url("https://jobapplicant.com"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token");
    }

    private String formatServiceName(String name) {
        if (name == null || name.isEmpty()) {
            return "Service";
        }
        // Convert "auth-service" to "Auth Service"
        return String.join(" ",
                java.util.Arrays.stream(name.split("-"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .toArray(String[]::new));
    }
}

