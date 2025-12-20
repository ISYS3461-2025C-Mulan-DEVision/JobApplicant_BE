package com.team.ja.auth.service;

import com.team.ja.auth.model.AuthCredential;

public interface EmailService {

    void sendActivationEmail(AuthCredential credential, String activationToken);
}
