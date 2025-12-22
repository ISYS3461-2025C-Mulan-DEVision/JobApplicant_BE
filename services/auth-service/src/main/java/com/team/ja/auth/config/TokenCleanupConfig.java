package com.team.ja.auth.config;

import com.team.ja.auth.repository.VerificationTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled job configuration for activation token cleanup.
 * Periodically removes expired verification tokens to keep the table lean
 * and prevent accumulation of unusable tokens.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupConfig {

    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * Cleanup job that runs every hour.
     *
     * Schedule explanation:
     * - fixedDelay = 60 minutes (in milliseconds)
     * - initialDelay = 2 minutes to allow the service to start up cleanly
     *
     * Behavior:
     * - Deletes all verification tokens whose expiryDate is in the past.
     */
    @Scheduled(initialDelay = 2 * 60 * 1000L, fixedDelay = 60 * 60 * 1000L)
    public void cleanupExpiredActivationTokens() {
        LocalDateTime cutoff = LocalDateTime.now();
        try {
            log.info("Starting expired activation token cleanup at {}", cutoff);
            verificationTokenRepository.deleteByExpiryDateBefore(cutoff);
            log.info("Expired activation token cleanup completed");
        } catch (Exception e) {
            log.warn("Expired token cleanup failed: {}", e.getMessage());
        }
    }
}
