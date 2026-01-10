package com.team.ja.auth.service;

import com.team.ja.auth.dto.request.ChangePasswordRequest;
import com.team.ja.auth.dto.request.LoginRequest;
import com.team.ja.auth.dto.request.RefreshTokenRequest;
import com.team.ja.auth.dto.request.RegisterRequest;
import com.team.ja.auth.dto.response.AuthResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {
    /**
     * Register a new user.
     * Creates auth credentials and sends activation email.
     */
    void register(RegisterRequest request);

    /**
     * Activate a user account using a verification token.
     */
    AuthResponse activateAccount(String token);

    /**
     * Resend activation email if the account is inactive or previous token expired.
     *
     * @param email the registered email address
     */
    void resendActivationEmail(String email);

    /**
     * Authenticate user and return tokens.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token.
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Validate if token is valid.
     */
    boolean validateToken(String token);

    /**
     * Logs out the user by blacklisting the provided token.
     * @param token The JWT token from the Authorization header.
     */
    void logout(String token);

    /**
     * Change user password.
     * Requires current password verification.
     * 
     * @param email User's email (extracted from JWT token)
     * @param request Change password request with current and new password
     */
    void changePassword(String email, ChangePasswordRequest request);
}
