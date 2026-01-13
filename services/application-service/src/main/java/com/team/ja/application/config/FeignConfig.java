package com.team.ja.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign Configuration for inter-service communication
 * Adds JWT/JWE token to all outgoing Feign client requests
 */
@Slf4j
@Configuration
public class FeignConfig {

    @Value("${feign.client.jm-service.token:#{null}}")
    private String jmServiceToken;

    /**
     * Interceptor to add Authorization header with JWE token
     * to all Feign client requests for Job Manager service
     */
    @Bean
    public RequestInterceptor jmServiceInterceptor() {
        return requestTemplate -> {
            if (jmServiceToken != null && !jmServiceToken.isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + jmServiceToken);
                log.debug("Added Authorization header to JM Service request");
            } else {
                log.warn("JM service token not configured, requests will not include authorization");
            }
        };
    }
}
