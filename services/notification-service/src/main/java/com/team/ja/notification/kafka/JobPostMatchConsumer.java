package com.team.ja.notification.kafka;

import com.team.ja.common.event.JobMatchedEvent;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for job matched events.
 * Listens to JOB_MATCHED topic (published by user-service) and creates notifications for premium users.
 * 
 * Flow:
 * 1. user-service publishes JobMatchedEvent to JOB_MATCHED topic when a job matches a premium user's search profile
 * 2. This consumer receives the event and creates a notification
 * 3. The notification is stored in the database and can be retrieved by the user
 * 
 * This implements Requirement 5.3.1 (Ultimo level): Real-time notification service
 * for job matching using Kafka messaging platform.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostMatchConsumer {

    private final NotificationService notificationService;

    /**
     * Handle job matched events from user-service.
     * Creates a notification for the premium user when a job post matches their search profile.
     * 
     * This event is published by user-service after:
     * - JobMatchingService evaluates a new job post against user search profiles
     * - User is confirmed to be a premium subscriber
     * - All matching criteria are satisfied (country, skills, salary, employment type, job title, fresher)
     *
     * @param event the job matched event from user-service containing job details and user ID
     */
    @KafkaListener(
            topics = KafkaTopics.JOB_MATCHED, 
            groupId = "${spring.kafka.consumer.group-id}", 
            containerFactory = "jobMatchedKafkaListenerContainerFactory"
    )
    public void handleJobMatched(JobMatchedEvent event) {
        log.info("Received JobMatchedEvent for premium user: {} and job post: {} ({})", 
                event.getUserId(), event.getJobPostId(), event.getJobTitle());

        try {
            // Create notification with job title and location details
            String jobTitle = event.getJobTitle() != null ? event.getJobTitle() : "a job post";
            String location = buildLocationString(event.getJobCity(), event.getJobCountryCode());
            
            // Add location to job title if available
            String fullJobInfo = location != null ? jobTitle + " in " + location : jobTitle;
            
            notificationService.createJobMatchNotification(
                    event.getUserId(),
                    event.getJobPostId().toString(),
                    fullJobInfo
            );
            
            log.info("Successfully created job match notification for premium user: {} for job: {} ({})", 
                    event.getUserId(), event.getJobPostId(), jobTitle);
        } catch (Exception e) {
            log.error("Failed to create job match notification for user: {} and job post: {} - Error: {}", 
                    event.getUserId(), event.getJobPostId(), e.getMessage(), e);
            // Don't rethrow - we don't want to block the consumer
            // The notification can be retried or the user can discover the job through search
        }
    }

    /**
     * Build a location string from city and country code.
     * 
     * @param city the city name (optional)
     * @param countryCode the country code (optional)
     * @return formatted location string, or null if both are missing
     */
    private String buildLocationString(String city, String countryCode) {
        if (city != null && !city.isEmpty() && countryCode != null && !countryCode.isEmpty()) {
            return city + ", " + countryCode;
        } else if (city != null && !city.isEmpty()) {
            return city;
        } else if (countryCode != null && !countryCode.isEmpty()) {
            return countryCode;
        }
        return null;
    }
}
