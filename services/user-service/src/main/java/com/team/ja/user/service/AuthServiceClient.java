package com.team.ja.user.service;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.UnauthorizedException;
import com.team.ja.user.dto.request.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for communicating with auth-service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8084}")
    private String authServiceUrl;

    /**
     * Change user password by calling auth-service.
     * 
     * @param email User's email
     * @param request Change password request
     * @throws UnauthorizedException if current password is incorrect
     * @throws BadRequestException if request is invalid
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Calling auth-service to change password for user: {}", email);

        String url = UriComponentsBuilder
            .fromUriString(authServiceUrl)
            .path("/api/v1/auth/change-password")
            .queryParam("email", email)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChangePasswordRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<ApiResponse<String>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Password changed successfully for user: {}", email);
            } else {
                log.error("Failed to change password for user: {}", email);
                throw new RuntimeException("Failed to change password");
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling auth-service to change password: {}", e.getMessage());
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                throw new UnauthorizedException("Current password is incorrect");
            } else if (status.value() == 400) {
                throw new BadRequestException("Invalid password change request: " + e.getMessage());
            } else {
                throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            log.error("Error calling auth-service to change password: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }
}

