package com.team.ja.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service to track failed login attempts within a time window using Redis.
 * Fulfills Requirement 2.2.2: blocking account after 5 failed attempts within 60 seconds.
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private static final String ATTEMPT_PREFIX = "login:attempts:";
    private static final int WINDOW_SECONDS = 60;

    /**
     * Increments the failed attempt count for the given email.
     * Sets a TTL of 60 seconds on the first failure in a window.
     *
     * @param email User email
     * @return The current attempt count in the window
     */
    public int incrementAttempts(String email) {
        String key = ATTEMPT_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }
        return attempts != null ? attempts.intValue() : 0;
    }

    /**
     * Resets the attempt counter for the given email.
     * Called after a successful login.
     *
     * @param email User email
     */
    public void resetAttempts(String email) {
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }
}
