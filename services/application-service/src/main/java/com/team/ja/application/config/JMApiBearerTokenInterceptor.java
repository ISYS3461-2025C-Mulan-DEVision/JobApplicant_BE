// File path: services/application-service/src/main/java/com/team/ja/application/config/JMApiBearerTokenInterceptor.java
package com.team.ja.application.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feign RequestInterceptor that adds Bearer token to all JM API requests.
 * Automatically injects the static Bearer token from environment variables.
 */
@Slf4j
@Component
public class JMApiBearerTokenInterceptor implements RequestInterceptor {

    @Value("${app.jm.api.bearer-token:}")
    private String bearerToken;

    @Override
    public void apply(RequestTemplate template) {
        // Only add token if it's configured and not empty
        if (bearerToken != null && !bearerToken.isEmpty() && !bearerToken.equals("your-static-bearer-token-here")) {
            log.debug("Adding Bearer token to JM API request: {}", template.path());
            template.header("Authorization", "Bearer " + bearerToken);
        } else {
            log.warn("JM API Bearer token not configured or using placeholder. Please set JM_API_BEARER_TOKEN in .env");
        }
    }
}
