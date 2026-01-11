package com.team.ja.notification.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for user registration events.
 * Creates welcome notifications for new users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeNotificationConsumer {

    private final NotificationService notificationService;

    /**
     * Handle user registered events.
     * Creates a welcome notification for the newly registered user.
     *
     * @param event the user registered event from auth-service
     */
    @KafkaListener(
            topics = KafkaTopics.USER_REGISTERED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {} ({})", event.getUserId(), event.getEmail());

        try {
            String firstName = event.getFirstName();
            notificationService.createWelcomeNotification(
                    event.getUserId(),
                    firstName != null ? firstName : "there"
            );
            log.info("Successfully created welcome notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to create welcome notification for user: {}", event.getUserId(), e);
            // Don't rethrow - we don't want to block the consumer
        }
    }
}

