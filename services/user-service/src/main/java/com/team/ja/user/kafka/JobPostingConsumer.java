package com.team.ja.user.kafka;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.JobMatchedEvent;
import com.team.ja.common.event.JobPostingEvent;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.user.model.User;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.JobMatchingService;
import com.team.ja.user.service.UserSearchProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for job posting events in user-service
 * 
 * Consumes from: jobpost.published, jobpost.skills.changed,
 * jobpost.country.changed
 * 
 * Evaluates each new job posting against all active search profiles
 * and publishes match notifications to notification-service via job-matched
 * topic
 * 
 * Only premium users' search profiles are eligible for job matching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingConsumer {

    private final JobMatchingService jobMatchingService;
    private final UserSearchProfileService userSearchProfileService;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, JobMatchedEvent> jobMatchedEventKafkaTemplate;

    /**
     * Handle new job posting event
     * Matches job against all active search profiles
     * Publishes notifications for matched profiles
     */
    @KafkaListener(topics = KafkaTopics.JOB_POST_PUBLISHED, groupId = "${spring.kafka.consumer.group-id:user-service}", containerFactory = "jobPostingEventKafkaListenerContainerFactory")
    public void handleJobPosted(JobPostingEvent jobEvent) {
        log.info("Received job posting event: jobId={}, title={}", jobEvent.getJobPostId(), jobEvent.getTitle());

        try {
            // Fetch all active search profiles
            List<UserSearchProfileUpdateEvent> activeProfiles = userSearchProfileService
                    .getAllActiveSearchProfilesAsEvents();
            log.debug("Processing job against {} active search profiles", activeProfiles.size());

            int matchCount = 0;
            // Evaluate job against each profile
            for (UserSearchProfileUpdateEvent profile : activeProfiles) {
                try {
                    // Check if user is premium - only premium users can receive job matches
                    User user = userRepository.findById(profile.getUserId()).orElse(null);
                    if (user == null || !user.isPremium()) {
                        log.debug("Skipping job match for user {} - user not found or not premium",
                                profile.getUserId());
                        continue;
                    }

                    if (jobMatchingService.isMatch(jobEvent, profile)) {
                        // Publish match event for notification-service
                        JobMatchedEvent matchEvent = JobMatchedEvent.builder()
                                .userId(profile.getUserId())
                                .jobPostId(jobEvent.getJobPostId())
                                .jobTitle(jobEvent.getTitle())
                                .jobCity(jobEvent.getCity())
                                .jobCountryCode(jobEvent.getCountryCode())
                                .matchedAt(LocalDateTime.now())
                                .build();

                        jobMatchedEventKafkaTemplate.send(KafkaTopics.JOB_MATCHED, matchEvent.getUserId().toString(),
                                matchEvent);
                        matchCount++;
                        log.info("Job {} matched with profile for premium user {}, sent to notification-service",
                                jobEvent.getJobPostId(), profile.getUserId());
                    }
                } catch (Exception e) {
                    log.error("Error matching job {} with profile for user {}", jobEvent.getJobPostId(),
                            profile.getUserId(), e);
                    // Continue processing other profiles
                }
            }

            log.info("Job posting {} matched {} search profiles", jobEvent.getJobPostId(), matchCount);

        } catch (Exception e) {
            log.error("Error processing job posting event for job {}", jobEvent.getJobPostId(), e);
        }
    }

    /**
     * Handle skill changes in job posting
     * Re-evaluate matching for affected search profiles
     */
    @KafkaListener(topics = KafkaTopics.JOB_POST_SKILL_CHANGE, groupId = "${spring.kafka.consumer.group-id:user-service}")
    public void handleJobSkillChanged(JobPostingEvent jobEvent) {
        log.info("Received job skill change event: jobId={}", jobEvent.getJobPostId());
        // Treat as a re-posting, evaluate against all profiles again
        handleJobPosted(jobEvent);
    }

    /**
     * Handle country changes in job posting
     * Re-evaluate matching for affected search profiles
     */
    @KafkaListener(topics = KafkaTopics.JOB_POST_COUNTRY_CHANGE, groupId = "${spring.kafka.consumer.group-id:user-service}")
    public void handleJobCountryChanged(JobPostingEvent jobEvent) {
        log.info("Received job country change event: jobId={}", jobEvent.getJobPostId());
        // Treat as a re-posting, evaluate against all profiles again
        handleJobPosted(jobEvent);
    }
}
