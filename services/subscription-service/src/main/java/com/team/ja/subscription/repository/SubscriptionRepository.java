package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.UserSubscription;
import com.team.ja.common.enumeration.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    UserSubscription findByUserIdAndIsActiveTrue(UUID userId);

    UserSubscription findByUserIdAndSubscriptionId(UUID userId, UUID subscriptionId);

    /**
     * Find subscriptions by user ID.
     */
    List<UserSubscription> findByUserId(UUID userId);

    /**
     * Find active subscriptions by user ID.
     */
    List<UserSubscription> findByUserIdAndSubscriptionStatus(UUID userId, SubscriptionStatus subscriptionStatus);

    /**
     * Check if an active subscription exists for a user.
     */
    boolean existsByUserIdAndSubscriptionStatus(UUID userId, SubscriptionStatus subscriptionStatus);

    /**
     * Find subscriptions that are expiring within a certain number of days.
     */
    List<UserSubscription> findBySubscriptionEndDateBeforeAndSubscriptionStatus(java.time.LocalDate date,
            SubscriptionStatus subscriptionStatus);

    /**
     * Find all subscriptions with a specific status.
     */
    List<UserSubscription> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);
}
