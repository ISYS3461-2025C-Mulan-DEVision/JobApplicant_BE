package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.Subscription;
import com.team.ja.common.enumeration.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Subscription findByUserIdAndIsActiveTrue(UUID userId);

    Subscription findByUserIdAndId(UUID userId, UUID subscriptionId);

    /**
     * Find subscriptions by user ID.
     */
    List<Subscription> findByUserId(UUID userId);

    /**
     * Find active subscriptions by user ID.
     */
    List<Subscription> findByUserIdAndSubscriptionStatus(UUID userId, SubscriptionStatus subscriptionStatus);

    /**
     * Check if an active subscription exists for a user.
     */
    boolean existsByUserIdAndSubscriptionStatus(UUID userId, SubscriptionStatus subscriptionStatus);

    /**
     * Find subscriptions that are expiring within a certain number of days.
     */
    List<Subscription> findBySubscriptionEndDateBeforeAndSubscriptionStatus(java.time.LocalDate date,
            SubscriptionStatus subscriptionStatus);

    /**
     * Find all subscriptions with a specific status.
     */
    List<Subscription> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);
}
