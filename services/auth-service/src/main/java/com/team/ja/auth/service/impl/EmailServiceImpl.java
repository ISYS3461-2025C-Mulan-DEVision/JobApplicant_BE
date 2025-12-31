package com.team.ja.auth.service.impl;

import com.team.ja.auth.model.AuthCredential;
import com.team.ja.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.url.activation}")
    private String activationBaseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendActivationEmail(
        AuthCredential credential,
        String activationToken
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(credential.getEmail());
            message.setSubject("Activate your JobApplicant account");

            // Build activation link assuming activationBaseUrl is the base origin.
            String base = activationBaseUrl != null ? activationBaseUrl.trim() : "";
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            
            String activationLink = String.format("%s/api/v1/auth/activate?token=%s", base, activationToken);
            
            String name = (credential.getUsername() != null && !credential.getUsername().isEmpty()) ? credential.getUsername() : "there";
            String emailContent = String.format(
                "Hi %s,\n\n" +
                    "Welcome to JobApplicant!\n\n" +
                    "To activate your account, please use the link below:\n\n" +
                    "%s\n\n" +
                    "If clicking doesn’t work, copy and paste the link into your browser.\n" +
                    "Note: This link expires in 24 hours.\n\n" +
                    "Thank you,\n" +
                    "The JobApplicant Team",
                name,
                activationLink
            );

            message.setText(emailContent);
            mailSender.send(message);
            log.info("Activation email sent to {}", credential.getEmail());
        } catch (MailException e) {
            log.error(
                "Failed to send activation email to {}: {}",
                credential.getEmail(),
                e.getMessage()
            );
            // Depending on requirements, you might re-throw, log and continue, or store for retry.
            // For now, we'll just log the error.
        }
    }
}
