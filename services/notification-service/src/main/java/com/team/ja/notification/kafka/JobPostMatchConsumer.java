package com.team.ja.notification.kafka;

import com.team.ja.common.event.JobMatchedEvent;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for job post match events.
 * Listens to JOB_POSTED_MATCHED topic and creates notifications for users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostMatchConsumer {

    private final NotificationService notificationService;

    /**
     * Handle job post match events.
     * Creates a notification for the user when a job post matches their search
     * profile.
     *
     * @param event the job post match event from subscription-service
     */
    @KafkaListener(topics = KafkaTopics.JOB_POSTED_MATCHED, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "jobPostMatchKafkaListenerContainerFactory")
    public void handleJobPostMatch(JobMatchedEvent event) {
        log.info("Received JobPostMatchEvent for user: {} and job post: {}", event.getUserId(), event.getJobPostId());

        try {
            notificationService.createJobMatchNotification(
                    event.getUserId(),
                    event.getJobPostId().toString(),
                    null // Job title not available in event, will use generic message
            );
            log.info("Successfully created job match notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to create job match notification for user: {} and job post: {}",
                    event.getUserId(), event.getJobPostId(), e);
            // Don't rethrow - we don't want to block the consumer
        }
    }
}
