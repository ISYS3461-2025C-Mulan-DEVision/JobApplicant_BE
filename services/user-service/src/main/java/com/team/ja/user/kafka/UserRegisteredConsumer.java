package com.team.ja.user.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.config.sharding.ShardingProperties;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.User;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.impl.ShardLookupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.checkerframework.checker.units.qual.s;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for user registration events.
 * Creates user profile when auth-service publishes registration event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredConsumer {

        private final UserRepository userRepository;
        private final CountryRepository countryRepository;

        /**
         * Handle user registered event.
         * Creates a new user profile with the same userId from auth-service.
         */
        @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "${spring.kafka.consumer.group-id}")
        public void handleUserRegistered(UserRegisteredEvent event) {

                log.info("Received user-registered event for userId: {} (Country: {})",
                                event.getUserId(), event.getCountryAbbreviation());

                String shardKey = ShardingProperties.resolveShard(event.getCountryAbbreviation());

                ShardContext.setShardKey(shardKey);
                log.info("Consumer routing thread to shard: {}", shardKey);

                try {
                        createProfileInShard(event);
                        log.info("User profile created successfully for userId: {}", event.getUserId());
                } catch (Exception e) {
                        log.error("Failed to sync user to shard {}: {}", shardKey, e.getMessage());
                } finally {
                        // 4. Always clear
                        ShardContext.clear();
                }
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void createProfileInShard(UserRegisteredEvent event) {
                // Check if user already exists (idempotency)
                if (userRepository.existsById(event.getUserId())) {
                        log.warn("User profile already exists for userId: {}, skipping creation.",
                                        event.getUserId());
                        return;
                }

                // Fetch country entity
                UUID countryId = null;
                String countryCode = event.getCountryAbbreviation();

                if (countryCode != null && !countryCode.isBlank()) {
                        countryId = countryRepository.findByAbbreviationIgnoreCase(countryCode)
                                        .map(Country::getId)
                                        .orElse(null);
                }
                // Create new user profile
                User user = User.builder()
                                .id(event.getUserId())
                                .email(event.getEmail())
                                .firstName(event.getFirstName())
                                .lastName(event.getLastName())
                                .countryId(countryId)
                                .phone(event.getPhone())
                                .address(event.getAddress())
                                .city(event.getCity())
                                .build();

                userRepository.save(user);
                log.info("User profile created in shard for userId: {}", event.getUserId());
        }

}
