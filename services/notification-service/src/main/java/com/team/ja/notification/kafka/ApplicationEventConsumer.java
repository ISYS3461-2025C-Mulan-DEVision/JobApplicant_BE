package com.team.ja.notification.kafka;

import com.team.ja.common.event.ApplicationCreatedEvent;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for application-related events.
 * Listens to APPLICATION_SUBMITTED topic and creates notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationEventConsumer {

    private final NotificationService notificationService;

    /**
     * Handle application created events.
     * Creates a notification for the user when their application is submitted.
     *
     * @param event the application created event from application-service
     */
    @KafkaListener(
            topics = KafkaTopics.APPLICATION_SUBMITTED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "applicationCreatedKafkaListenerContainerFactory"
    )
    public void handleApplicationCreated(ApplicationCreatedEvent event) {
        log.info("Received ApplicationCreatedEvent for user: {} and application: {}",
                event.getApplicantId(), event.getApplicationId());

        try {
            notificationService.createApplicationSubmittedNotification(
                    event.getApplicantId(),
                    event.getApplicationId(),
                    event.getJobPostId() != null ? event.getJobPostId().toString() : null
            );
            log.info("Successfully created application submitted notification for user: {}", event.getApplicantId());
        } catch (Exception e) {
            log.error("Failed to create application submitted notification for user: {} and application: {}",
                    event.getApplicantId(), event.getApplicationId(), e);
            // Don't rethrow - we don't want to block the consumer
        }
    }
}

