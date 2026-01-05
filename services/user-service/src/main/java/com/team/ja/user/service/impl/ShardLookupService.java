package com.team.ja.user.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.config.sharding.ShardingProperties;
import com.team.ja.user.model.User;
import com.team.ja.user.model.Country;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.global.CountryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShardLookupService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ShardingProperties shardingProperties;
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    private static final Duration CACHE_TTL = Duration.ofDays(30);
    private static final String USER_ID_SHARD_PREFIX = "user:shard:userId:";
    private static final String USER_EMAIL_SHARD_PREFIX = "user:shard:email:";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Looks up the shard ID for a given user ID.
     * First checks the Redis cache, if not found, queries the database and updates
     * the cache.
     */
    public String findShardIdByUserId(UUID userId) {
        String cacheKey = getCachedShard(userId);

        if (cacheKey != null) {
            return cacheKey;
        }

        String foundShard = scattergatherLookupById(userId);
        if (foundShard != null) {
            cachedUserIdShard(userId, foundShard);
        }
        return foundShard;
    }

    /**
     * Looks up the shard ID for a given user email.
     * First checks the Redis cache, if not found, queries the database and updates
     * 
     * @param email
     * @return
     */
    public String findShardByUserEmail(String email) {
        String cacheKey = getCachedShardEmail(email);
        if (cacheKey != null) {
            return cacheKey;
        }
        String foundShard = scattergatherLookupByEmail(email);
        if (foundShard != null) {
            cachedUserEmailShard(email, foundShard);
        }
        return foundShard;
    }

    public Optional<User> getUserById(UUID userId) {
        String shardId = findShardIdByUserId(userId);
        if (shardId == null) {
            log.warn("Shard ID not found for user ID: {}", userId);
            return Optional.empty();
        }

        ShardContext.setShardKey(shardId);
        try {
            return userRepository.findById(userId);
        } finally {
            ShardContext.clear();
        }
    }

    public Optional<User> getUserByEmail(String email) {
        String shardId = findShardByUserEmail(email);
        if (shardId == null) {
            log.warn("Shard ID not found for user email: {}", email);
            return Optional.empty();
        }

        ShardContext.setShardKey(shardId);
        try {
            return userRepository.findByEmailAndIsActiveTrue(email);
        } finally {
            ShardContext.clear();
        }
    }

    private String scattergatherLookupById(UUID userId) {
        List<String> shardKeys = new ArrayList<>(shardingProperties.getShards().keySet());

        for (String shardKey : shardKeys) {
            try {
                ShardContext.setShardKey(shardKey);
                boolean exists = userRepository.existsById(userId);
                if (exists) {
                    log.info("Found user ID: {} in shard: {}", userId, shardKey);
                    return shardKey;
                }
            } catch (Exception e) {
                log.error("Error querying shard: {} for user ID: {}", shardKey, userId, e);
            } finally {
                ShardContext.clear();
            }
        }
        log.warn("User ID: {} not found in any shard", userId);
        return null;
    }

    private String scattergatherLookupByEmail(String email) {
        List<String> shardKeys = new ArrayList<>(shardingProperties.getShards().keySet());

        for (String shardKey : shardKeys) {
            try {
                ShardContext.setShardKey(shardKey);
                Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
                if (userOpt.isPresent()) {
                    log.info("Found user email: {} in shard: {}", email, shardKey);
                    return shardKey;
                }
            } catch (Exception e) {
                log.error("Error querying shard: {} for user email: {}", shardKey, email, e);
            } finally {
                ShardContext.clear();
            }
        }
        log.warn("User email: {} not found in any shard", email);
        return null;
    }

    public boolean userIdExistsInAnyShard(UUID userId) {
        String cacheShard = getCachedShard(userId);
        if (cacheShard != null) {

            ShardContext.setShardKey(cacheShard);
            try {
                boolean exists = userRepository.existsById(userId);
                if (!exists) {
                    log.warn("User Id '{}' not found in shard '{}'", userId, cacheShard);
                    invalidateCache(userId);
                    return false;
                }
                return true;
            } finally {
                ShardContext.clear();
            }
        }

        return scattergatherLookupById(userId) != null;
    }

    public boolean userEmailExistsInAnyShard(String email) {
        String cacheShard = getCachedShardEmail(email);
        if (cacheShard != null) {

            ShardContext.setShardKey(cacheShard);
            try {
                Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(email);
                if (userOpt.isEmpty()) {
                    log.warn("User email '{}' not found in shard '{}'", email, cacheShard);
                    invalidateCacheEmail(email);
                    return false;
                }
                return true;
            } finally {
                ShardContext.clear();
            }
        }

        return scattergatherLookupByEmail(email) != null;
    }

    public void cachedUserIdShard(UUID userId, String shardId) {
        String cacheKey = USER_ID_SHARD_PREFIX + userId;
        redisTemplate.opsForValue().set(cacheKey, shardId, CACHE_TTL);
        log.info("Cached shard ID '{}' for user ID '{}'", shardId, userId);
    }

    public void cachedUserEmailShard(String email, String shardId) {
        String cacheKey = USER_EMAIL_SHARD_PREFIX + email;
        redisTemplate.opsForValue().set(cacheKey, shardId, CACHE_TTL);
        log.info("Cached shard ID '{}' for user email '{}'", shardId, email);
    }

    public String getCachedShard(UUID userId) {
        String cacheKey = USER_ID_SHARD_PREFIX + userId;
        return redisTemplate.opsForValue().get(cacheKey);
    }

    public String getCachedShardEmail(String email) {
        String cacheKey = USER_EMAIL_SHARD_PREFIX + email;
        return redisTemplate.opsForValue().get(cacheKey);
    }

    public void invalidateCache(UUID userId) {
        String cacheKey = USER_ID_SHARD_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated cache for user ID '{}'", userId);
    }

    public void invalidateCacheEmail(String email) {
        String cacheKey = USER_EMAIL_SHARD_PREFIX + email;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated cache for user email '{}'", email);
    }

    public void updateUserIdCache(UUID oldCountryID, UUID newCountryId, UUID userId) {
        executorService.submit(() -> {
            try {
                String currentShard = getCachedShard(userId);

                Country country = countryRepository.findById(newCountryId).orElse(null);

                String expectedShard = shardingProperties.getShardForCountry(country.getAbbreviation());

                if (currentShard != null && !currentShard.equals(expectedShard)) {
                    invalidateCache(userId);
                    cachedUserIdShard(userId, expectedShard);
                    log.info("Updated cache for user ID '{}' to new shard '{}'", userId, expectedShard);
                }
            } catch (Exception e) {
                log.error("Error updating cache for user ID '{}'", userId, e);
            }
        });
    }

    public void updateUserEmailCache(String oldEmail, String newEmail, UUID userId) {
        executorService.submit(() -> {
            try {
                String currentShard = getCachedShardEmail(oldEmail);

                Optional<User> userOpt = getUserById(userId);
                if (userOpt.isEmpty()) {
                    log.warn("User ID '{}' not found while updating email cache", userId);
                    return;
                }
                String expectedShard = findShardByUserEmail(newEmail);

                if (currentShard != null && !currentShard.equals(expectedShard)) {
                    invalidateCacheEmail(oldEmail);
                    cachedUserEmailShard(newEmail, expectedShard);
                    log.info("Updated cache for user email '{}' to new shard '{}'", newEmail, expectedShard);
                }
            } catch (Exception e) {
                log.error("Error updating cache for user email '{}'", newEmail, e);
            }
        });
    }
}
