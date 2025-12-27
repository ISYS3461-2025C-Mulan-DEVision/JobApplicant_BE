package com.team.ja.subscription.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.repository.SearchProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchProfileConsumer {

    private final SearchProfileRepository searchProfileRepository;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received user-registered event for userId: {}", event.getUserId());

        // Check if search profile already exists
        if (searchProfileRepository.existsByUserId(event.getUserId())) {
            log.warn("Search profile already exists for userId: {}. Skipping.", event.getUserId());
            return;
        }

        // Create default search profile
        SearchProfile profile = new SearchProfile();
        profile.setUserId(event.getUserId());
        profile.setJobTitle(null);
        profile.setCountryId(null);
        profile.setEmployments(null);
        profile.setSalaryMin(null);
        profile.setSalaryMax(null);
        profile.setSkills(null);
        // Set the profile as active by default
        profile.setActive(true);
        searchProfileRepository.save(profile);

        log.info("Default search profile created for userId: {}", event.getUserId());
    }
}
