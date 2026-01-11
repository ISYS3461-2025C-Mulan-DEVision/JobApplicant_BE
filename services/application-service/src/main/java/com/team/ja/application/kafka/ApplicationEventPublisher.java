// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\kafka\ApplicationEventPublisher.java

package com.team.ja.application.kafka;

import com.team.ja.common.event.ApplicationCreatedEvent;
import com.team.ja.common.event.ApplicationWithdrawnByApplicantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event publisher for application-service.
 * Publishes events related to job applications using fire-and-forget pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventPublisher {

    private final KafkaTemplate<String, ApplicationCreatedEvent> applicationCreatedKafkaTemplate;
    private final KafkaTemplate<String, ApplicationWithdrawnByApplicantEvent> applicationWithdrawnKafkaTemplate;

    // Topic names for application events
    private static final String APPLICATION_CREATED_TOPIC = "application-created-events";
    private static final String APPLICATION_WITHDRAWN_TOPIC = "application-withdrawn-events";

    /**
     * Publishes an ApplicationCreatedEvent when a new application is created.
     * Uses fire-and-forget pattern (async, no response expected).
     *
     * @param event The ApplicationCreatedEvent to publish
     */
    public void publishApplicationCreatedEvent(ApplicationCreatedEvent event) {
        try {
            // Use applicationId as the key for Kafka partitioning
            String key = event.getApplicationId().toString();

            // Send message asynchronously (fire-and-forget)
            // Topic, key, and message are sent directly
            applicationCreatedKafkaTemplate.send(APPLICATION_CREATED_TOPIC, key, event);

            log.info("Published ApplicationCreatedEvent - eventId: {}, applicationId: {}, applicantId: {}, jobPostId: {}",
                    event.getEventId(), event.getApplicationId(), event.getApplicantId(), event.getJobPostId());

        } catch (Exception e) {
            // Log error but don't throw - fire-and-forget pattern
            // Other services can retry from the topic if needed
            log.error("Failed to publish ApplicationCreatedEvent - eventId: {}, applicationId: {}",
                    event.getEventId(), event.getApplicationId(), e);
        }
    }

    /**
     * Publishes an ApplicationWithdrawnByApplicantEvent when an applicant withdraws their application.
     * Uses fire-and-forget pattern (async, no response expected).
     *
     * @param event The ApplicationWithdrawnByApplicantEvent to publish
     */
    public void publishApplicationWithdrawnEvent(ApplicationWithdrawnByApplicantEvent event) {
        try {
            // Use applicationId as the key for Kafka partitioning
            String key = event.getApplicationId().toString();

            // Send message asynchronously (fire-and-forget)
            applicationWithdrawnKafkaTemplate.send(APPLICATION_WITHDRAWN_TOPIC, key, event);

            log.info("Published ApplicationWithdrawnByApplicantEvent - eventId: {}, applicationId: {}, applicantId: {}, jobPostId: {}",
                    event.getEventId(), event.getApplicationId(), event.getApplicantId(), event.getJobPostId());

        } catch (Exception e) {
            // Log error but don't throw - fire-and-forget pattern
            // Other services can retry from the topic if needed
            log.error("Failed to publish ApplicationWithdrawnByApplicantEvent - eventId: {}, applicationId: {}",
                    event.getEventId(), event.getApplicationId(), e);
        }
    }
}
