package com.team.ja.auth.service;

import com.team.ja.auth.model.AuthCredential;

public interface EmailService {

    /**
     * Sends account activation email with verification link.
     * 
     * @param credential User's auth credential
     * @param activationToken Activation token
     */
    void sendActivationEmail(AuthCredential credential, String activationToken);

    /**
     * Sends password reset email with reset link.
     * 
     * @param credential User's auth credential
     * @param resetToken Password reset token
     */
    void sendPasswordResetEmail(AuthCredential credential, String resetToken);
}
