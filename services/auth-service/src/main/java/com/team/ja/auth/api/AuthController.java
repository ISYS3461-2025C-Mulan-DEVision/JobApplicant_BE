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
    @Operation(summary = "Health check", description = "Check if auth service is running")
    public ApiResponse<String> health() {
        return ApiResponse.success("Auth Service is running");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register", description = "Register a new user account")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success("Registration successful", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and get access tokens")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success("Token refreshed", response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if a token is valid")
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ApiResponse.success(isValid);
    }
}
