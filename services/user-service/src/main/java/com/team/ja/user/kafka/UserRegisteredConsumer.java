package com.team.ja.user.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserProfileCreateEvent;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.config.sharding.ShardingProperties;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for user registration events.
 * Creates user profile when auth-service publishes registration event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredConsumer {

        private final com.team.ja.user.service.impl.UserProfileRegistrationService registrationService;

        /**
         * Handle user registered event.
         * Creates a new user profile with the same userId from auth-service.
         * 
         * NOTE: Exceptions are rethrown to allow Kafka to retry failed messages.
         */
        @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "user-service-user-registered-group")
        public void handleUserRegistered(UserRegisteredEvent event) {

                log.info("Received user-registered event for userId: {} (Country: {})",
                                event.getUserId(), event.getCountryAbbreviation());

                String shardKey = ShardingProperties.resolveShard(event.getCountryAbbreviation());

                ShardContext.setShardKey(shardKey);
                log.info("Consumer routing thread to shard: {}", shardKey);

                try {
                        registrationService.saveProfileInShard(event, shardKey);
                        log.info("User profile created successfully for userId: {} in shard: {}", event.getUserId(), shardKey);
                } catch (Exception e) {
                        log.error("Failed to sync user to shard {}: {}", shardKey, e.getMessage(), e);
                        // Rethrow to allow Kafka to retry the message
                        throw e;
                } finally {
                        ShardContext.clear();
                }
        }

}
