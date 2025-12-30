package com.team.ja.user.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.user.model.User;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    /**
     * Handle user registered event.
     * Creates a new user profile with the same userId from auth-service.
     */
    @KafkaListener(
        topics = KafkaTopics.USER_REGISTERED,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info(
            "Received user-registered event for userId: {}",
            event.getUserId()
        );

        // Check if user already exists (idempotency)
        if (userRepository.existsById(event.getUserId())) {
            log.warn(
                "User already exists with userId: {}. Skipping.",
                event.getUserId()
            );
            return;
        }

        // Resolve country abbreviation to countryId (may be null if not found)
        java.util.UUID countryId = null;
        if (
            event.getCountryAbbreviation() != null &&
            !event.getCountryAbbreviation().isBlank()
        ) {
            countryId = countryRepository
                .findByAbbreviation(event.getCountryAbbreviation())
                .map(com.team.ja.user.model.Country::getId)
                .orElse(null);
        }

        // Create user profile with extended fields
        User user = User.builder()
            .id(event.getUserId()) // Use same ID from auth-service
            .email(event.getEmail())
            .firstName(event.getFirstName())
            .lastName(event.getLastName())
            .phone(event.getPhone())
            .address(event.getAddress())
            .city(event.getCity())
            .countryId(countryId)
            .build();

        userRepository.save(user);
        log.info("User profile created for userId: {}", event.getUserId());
    }
}
