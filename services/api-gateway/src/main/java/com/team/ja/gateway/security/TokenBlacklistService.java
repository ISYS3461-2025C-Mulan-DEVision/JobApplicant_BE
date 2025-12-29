package com.team.ja.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for checking the JWT blacklist in Redis.
 * Uses reactive Redis template for non-blocking I/O.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveStringRedisTemplate reactiveRedisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * Checks if a token's JTI is in the blacklist.
     * @param jti The JTI (JWT ID) of the token to check.
     * @return A Mono<Boolean> that emits true if the token is blacklisted.
     */
    public Mono<Boolean> isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return Mono.just(false);
        }
        String key = BLACKLIST_PREFIX + jti;
        return reactiveRedisTemplate.hasKey(key);
    }
}
