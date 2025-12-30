package com.team.ja.auth.api;

import com.team.ja.auth.dto.request.LoginRequest;
import com.team.ja.auth.dto.request.RefreshTokenRequest;
import com.team.ja.auth.dto.request.RegisterRequest;
import com.team.ja.auth.dto.response.AuthResponse;
import com.team.ja.auth.service.AuthService;
import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 * Handles registration, login, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if auth service is running"
    )
    public ApiResponse<String> health() {
        return ApiResponse.success("Auth Service is running");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register",
        description = "Register a new user account. Required fields: email, password, firstName, lastName, country (2-letter abbreviation). Optional: phone, address (street), city. Sends an activation email to complete registration.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                    implementation = com.team.ja.auth.dto.request
                        .RegisterRequest.class
                ),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Register example",
                        value = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"Password123!\",\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"country\": \"US\",\n  \"phone\": \"+84123456789\",\n  \"address\": \"123 Nguyen Hue Street\",\n  \"city\": \"Ho Chi Minh City\"\n}"
                    ),
                }
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Registration initiated. Activation email sent.",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Registration initiated",
                        value = "{\n  \"success\": true,\n  \"message\": \"Registration successful. Please check your email to activate your account.\",\n  \"timestamp\": \"2025-01-01T10:00:00\"\n}"
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Email already registered"
            ),
        }
    )
    public ApiResponse<String> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ApiResponse.success(
            "Registration successful. Please check your email to activate your account."
        );
    }

    @GetMapping("/activate")
    @Operation(
        summary = "Activate account",
        description = "Activate a user account using the verification token received via email. On success, returns access and refresh tokens.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Account activated; tokens issued",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Activation success",
                        value = "{\n  \"success\": true,\n  \"message\": \"Account activated successfully\",\n  \"data\": {\n    \"accessToken\": \"<jwt>\",\n    \"refreshToken\": \"<jwt>\",\n    \"tokenType\": \"Bearer\",\n    \"expiresIn\": 3600,\n    \"userId\": \"11111111-1111-1111-1111-111111111111\",\n    \"email\": \"john.doe@example.com\",\n    \"role\": \"FREE\"\n  },\n  \"timestamp\": \"2025-01-01T10:00:00\"\n}"
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid or expired activation token"
            ),
        }
    )
    public ApiResponse<AuthResponse> activateAccount(
        @RequestParam String token
    ) {
        AuthResponse response = authService.activateAccount(token);
        return ApiResponse.success("Account activated successfully", response);
    }

    @PostMapping("/resend-activation")
    @Operation(
        summary = "Resend activation email",
        description = "Resend activation email if previous token expired or the email was lost"
    )
    public ApiResponse<String> resendActivation(@RequestParam String email) {
        authService.resendActivationEmail(email);
        return ApiResponse.success(
            "If the account is inactive, a new activation email has been sent."
        );
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Authenticate and get access tokens"
    )
    public ApiResponse<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token",
        description = "Get new access token using refresh token"
    )
    public ApiResponse<AuthResponse> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success("Token refreshed", response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Invalidate the current user's access token"
    )
    public ApiResponse<String> logout(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        if (
            authorizationHeader != null &&
            authorizationHeader.startsWith("Bearer ")
        ) {
            String token = authorizationHeader.substring(7);
            authService.logout(token);
            return ApiResponse.success("Logged out successfully");
        }
        return ApiResponse.error("Invalid authorization header");
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validate token",
        description = "Check if a token is valid"
    )
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ApiResponse.success(isValid);
    }
}
