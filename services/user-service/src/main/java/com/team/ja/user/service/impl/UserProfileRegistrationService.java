package com.team.ja.user.service.impl;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserProfileCreateEvent;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileRegistrationService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final UserSearchProfileRepository userSearchProfileRepository;
    private final KafkaTemplate<String, UserProfileCreateEvent> kafkaTemplate;
    private final ShardLookupService shardLookupService;

    @Transactional
    public void saveProfileInShard(UserRegisteredEvent event, String shardKey) {
        if (userRepository.existsById(event.getUserId())) {
            log.warn("User profile already exists for userId: {}, skipping.", event.getUserId());
            return;
        }

        UUID countryId = null;
        String countryCode = event.getCountryAbbreviation();

        if (countryCode != null && !countryCode.isBlank()) {
            countryId = countryRepository.findByAbbreviationIgnoreCase(countryCode)
                    .map(Country::getId).orElse(null);
        }

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
        
        // Cache the shard location for this user immediately
        shardLookupService.cachedUserIdShard(user.getId(), shardKey);
        shardLookupService.cachedUserEmailShard(user.getEmail(), shardKey);
        
        log.info("User saved for userId: {}", event.getUserId());

        UserSearchProfile userSearchProfile = new UserSearchProfile();
        userSearchProfile.setUserId(user.getId());
        userSearchProfile.setCountryAbbreviation(event.getCountryAbbreviation());
        userSearchProfileRepository.save(userSearchProfile);
        log.info("User search profile saved for userId: {}", event.getUserId());

        // Notify other systems
        UserProfileCreateEvent profileEvent = UserProfileCreateEvent.builder()
                .userId(event.getUserId())
                .countryAbbreviation(event.getCountryAbbreviation())
                .educationLevel(null)
                .skillIds(null)
                .minSalary(null)
                .maxSalary(null)
                .employmentTypes(null)
                .jobTitles(null)
                .build();
        
        kafkaTemplate.send(KafkaTopics.USER_PROFILE_CREATE, profileEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent UserProfileCreateEvent for user {} [partition: {}, offset: {}]", 
                                event.getUserId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send UserProfileCreateEvent for user {}", event.getUserId(), ex);
                    }
                });
    }
}
