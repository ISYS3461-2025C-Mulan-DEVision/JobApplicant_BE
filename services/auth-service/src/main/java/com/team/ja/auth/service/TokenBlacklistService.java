package com.team.ja.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * Service for managing the JWT blacklist in Redis.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * Adds a token's JTI to the blacklist until it expires.
     * @param jti The JTI (JWT ID) of the token to blacklist.
     * @param expiration The expiration date of the token.
     */
    public void blacklistToken(String jti, Date expiration) {
        long remainingValidity = expiration.getTime() - System.currentTimeMillis();
        if (remainingValidity > 0) {
            String key = BLACKLIST_PREFIX + jti;
            redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(remainingValidity));
        }
    }

    /**
     * Checks if a token's JTI is in the blacklist.
     * @param jti The JTI (JWT ID) of the token to check.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
