package com.team.ja.subscription.config;

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

    @Value("${feign.client.payment-service.token:#{null}}")
    private String paymentServiceToken;

    /**
     * Interceptor to add Authorization header with JWE token
     * to all Feign client requests
     */
    @Bean
    public RequestInterceptor paymentServiceInterceptor() {
        return requestTemplate -> {
            if (paymentServiceToken != null && !paymentServiceToken.isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + paymentServiceToken);
                log.debug("Added Authorization header to Payment Service request");
            } else {
                log.warn("Payment service token not configured, requests will not include authorization");
            }
        };
    }
}
