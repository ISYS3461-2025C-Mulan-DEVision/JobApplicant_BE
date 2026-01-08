package com.team.ja.subscription.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.mapper.SubscriptionMapper;
import com.team.ja.subscription.model.UserSubscription;
import com.team.ja.subscription.repository.SubscriptionRepository;
import com.team.ja.common.enumeration.SubscriptionStatus;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of SubscriptionService.
 * Handles creation, updating, and deactivation of user subscriptions.
 * Deactivation is performed automatically for expired or cancelled
 * subscriptions.
 */
@Service
@RequiredArgsConstructor
@EnableScheduling
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final KafkaTemplate<String, SubscriptionDeactivateEvent> kafkaTemplate;

    @Override
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        // Always create a new subscription record. Do NOT reactivate old records.
        UserSubscription subscription = subscriptionMapper.toEntity(request);
        if (subscription.getSubscriptionStatus() == null) {
            subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        }

        // Set created timestamps
        subscription.setCreatedAt(LocalDateTime.now());
        // Set updated timestamps
        subscription.setUpdatedAt(LocalDateTime.now());
        // Start and end date will be set by the payment
        // processing flow
        subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        subscription.setActive(true);
        UserSubscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse update(UUID id, UpdateSubscriptionRequest request) {
        UserSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription", "id", id.toString()));

        if (request.getSubscriptionStatus() != null)
            subscription.setSubscriptionStatus(request.getSubscriptionStatus());
        if (request.getSubscriptionStartDate() != null)
            subscription.setSubscriptionStartDate(request.getSubscriptionStartDate());
        if (request.getSubscriptionEndDate() != null)
            subscription.setSubscriptionEndDate(request.getSubscriptionEndDate());
        UserSubscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(saved);
    }

    /**
     * Deactivate (soft-delete) a subscription by its ID.
     * Use this method to automatically deactivate subscriptions instead of deleting
     * them.
     * This methods should be called periodically (e.g., daily) to clean up expired
     * subscriptions.
     */
    @Override
    @Transactional
    // @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight (set this for
    // production use)
    @Scheduled(cron = "0 0/5 * * * ?") // Runs every 5 minutes (for testing purposes)
    public void deactivate() {
        List<UserSubscription> subscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.CANCELLED);
        for (UserSubscription cancelSubscription : subscriptions) {
            if (cancelSubscription.getSubscriptionEndDate().isBefore(LocalDate.now())) {
                cancelSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                cancelSubscription.setActive(false);
                subscriptionRepository.save(cancelSubscription);

                // TODO: Add notification logic to inform users about deactivated subscriptions
            }
        }

        List<UserSubscription> activeSubscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        for (UserSubscription activeSubscription : activeSubscriptions) {
            if (activeSubscription.getSubscriptionEndDate().isBefore(LocalDate.now())) {
                activeSubscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
                activeSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                activeSubscription.setActive(false);
                subscriptionRepository.save(activeSubscription);

                // TODO: Add notification logic to inform users about expired subscriptions

                // Publish subscription deactivation event to notify other services
                kafkaTemplate.send(KafkaTopics.SUBSCRIPTION_DEACTIVATE, SubscriptionDeactivateEvent.builder()
                        .payerId(activeSubscription.getUserId())
                        .build());
            }
        }

        List<UserSubscription> expiredSubscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.EXPIRED);
        for (UserSubscription expiredSubscription : expiredSubscriptions) {
            if (expiredSubscription.getDeactivatedAt() == null) {
                expiredSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                expiredSubscription.setActive(false);
                subscriptionRepository.save(expiredSubscription);
                // TODO: Add notification logic to inform users about expired subscriptions

                // Publish subscription deactivation event to notify other services
                kafkaTemplate.send(KafkaTopics.SUBSCRIPTION_DEACTIVATE, SubscriptionDeactivateEvent.builder()
                        .payerId(expiredSubscription.getUserId())
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void userDeactivate(UUID id) {
        UserSubscription subscription = subscriptionRepository.findByUserIdAndIsActiveTrue(id);

        if (subscription == null) {
            throw new NotFoundException("Active subscription", "userId", id.toString());
        }

        if (subscription.getSubscriptionEndDate().isBefore(LocalDate.now())) {
            subscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            subscription.setDeactivatedAt(LocalDateTime.now());
            // Invalidate the subscription
            subscription.setActive(false);
        } else {
            subscription.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
            // Let the scheduler handle deactivation after end date
            // TODO: Add notification logic to inform users about cancelled subscriptions
        }
        subscriptionRepository.save(subscription);
    }

    @Override
    public UserSubscription userReactivate(UUID userId, UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findByUserIdAndSubscriptionId(userId, subscriptionId);

        if (subscription == null) {
            throw new NotFoundException("Active subscription", "userId", userId.toString());
        }

        if (subscription.getSubscriptionStatus() != SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Subscription is not in a cancellable state.");
        }

        if (subscription.getSubscriptionEndDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Cannot reactivate an expired subscription.");
        }

        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        return subscription;
    }
}
