package com.team.ja.notification.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.notification.dto.request.CreateNotificationRequest;
import com.team.ja.notification.enumeration.NotificationType;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for subscription-related events.
 * Listens to subscription activation/deactivation topics and creates notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionEventConsumer {

    private final NotificationService notificationService;

    /**
     * Handle subscription activation events.
     * Creates a notification when user's premium subscription is activated.
     *
     * @param event the subscription activation event
     */
    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_ACTIVATE,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "subscriptionActivateKafkaListenerContainerFactory"
    )
    public void handleSubscriptionActivated(SubscriptionActivateEvent event) {
        log.info("Received SubscriptionActivateEvent for user: {}", event.getPayerId());

        try {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(event.getPayerId())
                    .notificationType(NotificationType.SUBSCRIPTION_ACTIVATED)
                    .title("Premium Subscription Activated!")
                    .message("Welcome to Premium! You now have access to real-time job notifications matching your search profile.")
                    .build();

            notificationService.createNotification(request);
            log.info("Successfully created subscription activated notification for user: {}", event.getPayerId());
        } catch (Exception e) {
            log.error("Failed to create subscription activated notification for user: {}", event.getPayerId(), e);
        }
    }

    /**
     * Handle subscription deactivation events.
     * Creates a notification when user's premium subscription is deactivated.
     *
     * @param event the subscription deactivation event
     */
    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_DEACTIVATE,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "subscriptionDeactivateKafkaListenerContainerFactory"
    )
    public void handleSubscriptionDeactivated(SubscriptionDeactivateEvent event) {
        log.info("Received SubscriptionDeactivateEvent for user: {}", event.getPayerId());

        try {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(event.getPayerId())
                    .notificationType(NotificationType.SUBSCRIPTION_DEACTIVATED)
                    .title("Premium Subscription Expired")
                    .message("Your premium subscription has expired. Renew to continue receiving real-time job notifications.")
                    .build();

            notificationService.createNotification(request);
            log.info("Successfully created subscription deactivated notification for user: {}", event.getPayerId());
        } catch (Exception e) {
            log.error("Failed to create subscription deactivated notification for user: {}", event.getPayerId(), e);
        }
    }
}

