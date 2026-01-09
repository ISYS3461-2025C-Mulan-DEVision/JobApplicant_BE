package com.team.ja.subscription.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.model.Subscription;
import com.team.ja.subscription.repository.SubscriptionRepository;
import com.team.ja.common.enumeration.SubscriptionStatus;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

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
    private final KafkaTemplate<String, SubscriptionDeactivateEvent> kafkaDeactivateTemplate;

    @Override
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        // Always create a new subscription record. Do NOT reactivate old records.
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setSubscriptionStartDate(request.getSubscriptionStartDate());
        subscription.setSubscriptionEndDate(request.getSubscriptionEndDate());

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
        Subscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse update(UUID id, UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription", "id", id.toString()));

        if (request.getSubscriptionStatus() != null)
            subscription.setSubscriptionStatus(request.getSubscriptionStatus());
        if (request.getSubscriptionStartDate() != null)
            subscription.setSubscriptionStartDate(request.getSubscriptionStartDate());
        if (request.getSubscriptionEndDate() != null)
            subscription.setSubscriptionEndDate(request.getSubscriptionEndDate());
        Subscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
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
        List<Subscription> subscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.CANCELLED);
        for (Subscription cancelSubscription : subscriptions) {
            if (cancelSubscription.getSubscriptionEndDate().isBefore(LocalDate.now())) {
                cancelSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                cancelSubscription.setActive(false);
                subscriptionRepository.save(cancelSubscription);

                // TODO: Add notification logic to inform users about deactivated subscriptions
            }
        }

        List<Subscription> activeSubscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        for (Subscription activeSubscription : activeSubscriptions) {
            if (activeSubscription.getSubscriptionEndDate().isBefore(LocalDate.now())) {
                activeSubscription.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
                activeSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                activeSubscription.setActive(false);
                subscriptionRepository.save(activeSubscription);

                // TODO: Add notification logic to inform users about expired subscriptions

                // Publish subscription deactivation event to notify other services
                kafkaDeactivateTemplate.send(KafkaTopics.SUBSCRIPTION_DEACTIVATE, SubscriptionDeactivateEvent.builder()
                        .payerId(activeSubscription.getUserId())
                        .build());
            }
        }

        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findBySubscriptionStatus(SubscriptionStatus.EXPIRED);
        for (Subscription expiredSubscription : expiredSubscriptions) {
            if (expiredSubscription.getDeactivatedAt() == null) {
                expiredSubscription.setDeactivatedAt(LocalDateTime.now());
                // Invalidate the subscription
                expiredSubscription.setActive(false);
                subscriptionRepository.save(expiredSubscription);
                // TODO: Add notification logic to inform users about expired subscriptions

                // Publish subscription deactivation event to notify other services
                kafkaDeactivateTemplate.send(KafkaTopics.SUBSCRIPTION_DEACTIVATE, SubscriptionDeactivateEvent.builder()
                        .payerId(expiredSubscription.getUserId())
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public void userDeactivate(UUID id) {
        Subscription subscription = subscriptionRepository.findByUserIdAndIsActiveTrue(id);

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
    public Subscription userReactivate(UUID userId, UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndId(userId, subscriptionId);

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

    private SubscriptionResponse mapToResponse(Subscription s) {
        if (s == null)
            return null;
        SubscriptionResponse resp = new SubscriptionResponse();
        resp.setId(s.getId());
        resp.setUserId(s.getUserId());
        resp.setSubscriptionStatus(s.getSubscriptionStatus());
        resp.setSubscriptionStartDate(s.getSubscriptionStartDate());
        resp.setSubscriptionEndDate(s.getSubscriptionEndDate());
        return resp;
    }
}
