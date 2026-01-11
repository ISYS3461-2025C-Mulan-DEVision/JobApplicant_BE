package com.team.ja.auth.model;

/**
 * Enum for verification token types.
 */
public enum TokenType {
    /**
     * Token for email verification during registration.
     */
    ACTIVATION,
    
    /**
     * Token for password reset via forgot password flow.
     */
    PASSWORD_RESET
}

