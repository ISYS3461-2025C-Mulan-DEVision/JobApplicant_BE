package com.team.ja.subscription.kafka;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.common.enumeration.SubscriptionStatus;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.PaymentCompletedEvent;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.subscription.model.Subscription;
import com.team.ja.subscription.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Producer for subscription-related events.
 * This class is intended to handle the publishing subscription status to
 * services that process this info (e.g., notification-service, user and auth
 * service).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionConsumer {

    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, SubscriptionActivateEvent> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_COMPLETED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentResponse(PaymentCompletedEvent event) {

        log.info("Received payment completed response for subscription processing: {}", event);
        // Fetch user subscriptions, this should have 1 active subscription and possibly
        // old inactive ones
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdAndSubscriptionStatus(event.getPayerId(), SubscriptionStatus.ACTIVE);

        // If no active subscription found, create one, this should not happen normally
        if (subscriptions.isEmpty()) {
            log.warn("No active subscriptions found for user ID: {}", event.getPayerId());
            Subscription subscription = new Subscription();
            // Set user ID
            subscription.setUserId(event.getPayerId());
            // Set subscription status (completed = active)
            subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            // Set start date to today
            subscription.setSubscriptionStartDate(LocalDate.now());
            // Set end date to 1 month from today
            subscription.setSubscriptionEndDate(LocalDate.now().plusMonths(1));
            // Set created timestamps
            subscription.setCreatedAt(LocalDateTime.now());
            // Set updated timestamps
            subscription.setUpdatedAt(LocalDateTime.now());
            // Save new subscription
            subscriptionRepository.save(subscription);
            log.info("Created new active subscription for user ID: {}", event.getPayerId());

        }

        // If active subscription(s) found, extend the end date by 1 month
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                if (subscription.getSubscriptionEndDate() != null) {
                    LocalDate oldEndDate = subscription.getSubscriptionEndDate();
                    subscription.setSubscriptionEndDate(oldEndDate.plusMonths(1));
                    // Update updated timestamp
                    subscription.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(subscription);
                    log.info("Extended subscription ID: {} end date from {} to {}",
                            subscription.getId(), oldEndDate, subscription.getSubscriptionEndDate());
                } else {
                    if (subscription.getSubscriptionStartDate() == null && subscription.getSubscriptionEndDate() == null
                            && subscription.getSubscriptionStatus() == SubscriptionStatus.PENDING) {
                        subscription.setSubscriptionStartDate(LocalDate.now());
                        subscription.setSubscriptionEndDate(LocalDate.now().plusMonths(1));
                        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                        // Update updated timestamp
                        subscription.setUpdatedAt(LocalDateTime.now());
                        subscriptionRepository.save(subscription);
                    }
                }
                log.info("Updated subscription ID: {} for user ID: {}",
                        subscription.getId(), event.getPayerId());
            }

        }

        // Publish subscription changed event to notify other services
        kafkaTemplate.send(KafkaTopics.SUBSCRIPTION_ACTIVATE, SubscriptionActivateEvent.builder()
                .payerId(event.getPayerId())
                .build())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent SubscriptionActivateEvent for user {} [partition: {}, offset: {}]",
                                event.getPayerId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send SubscriptionActivateEvent for user {}", event.getPayerId(), ex);
                    }
                });
        log.info("Published premium activation event for user ID: {}",
                event.getPayerId());

    }

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_FAILED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentFailed(PaymentCompletedEvent event) {

        log.info("Received payment failed response for subscription processing: {}", event);
        // Fetch user subscriptions, this should have 1 active subscription and possibly
        // old inactive ones
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdAndSubscriptionStatus(event.getPayerId(), SubscriptionStatus.ACTIVE);

        // If active subscription found, set to INACTIVE
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                if (subscription.getSubscriptionEndDate() != LocalDate.now()
                        && subscription.getSubscriptionEndDate().isAfter(LocalDate.now())) {
                    // TODO: Implement grace period notification logic here
                    log.info(
                            "Subscription ID: {} is still within valid period until {}, consider grace period handling",
                            subscription.getId(), subscription.getSubscriptionEndDate());
                    subscription.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(subscription);
                }
                log.info("Deactivated subscription ID: {} due to payment failure",
                        subscription.getId());
            }
        } else {
            log.warn("No active subscriptions found for user ID: {}",
                    event.getPayerId());
        }
    }

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_CANCELLED, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCancelled(PaymentCompletedEvent event) {

        log.info("Received payment cancelled response for subscription processing: {}", event);
        // Fetch user subscriptions, this should have 1 active subscription and possibly
        // old inactive ones
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdAndSubscriptionStatus(event.getPayerId(), SubscriptionStatus.ACTIVE);

        // If active subscription found, check for subscription end date and notify user
        // This is a hard cancellation, so we set to INACTIVE
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                if (subscription.getSubscriptionEndDate() != LocalDate.now()
                        && subscription.getSubscriptionEndDate().isAfter(LocalDate.now())) {
                    // TODO: Implement cancellation notification logic here
                    log.info(
                            "Subscription ID: {} is still within valid period until {}, notifying user of cancellation",
                            subscription.getId(), subscription.getSubscriptionEndDate());
                    subscription.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(subscription);
                }
            }
        } else {
            log.warn("No active subscriptions found for user ID: {}",
                    event.getPayerId());
        }
    }
}