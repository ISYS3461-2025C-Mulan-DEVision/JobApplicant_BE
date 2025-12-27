// package com.team.ja.subscription.repository;

// import java.util.List;
// import java.util.UUID;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import com.team.ja.subscription.model.subscription.UserSubscription;

// /**
// * Repository for UserSubscription entity.
// */
// @Repository
// public interface UserSubscriptionRepository extends
// JpaRepository<UserSubscription, UUID> {

// /**
// * Find subscriptions by user ID.
// */
// List<UserSubscription> findByUserId(UUID userId);

// /**
// * Find active subscriptions by user ID.
// */
// List<UserSubscription> findByUserIdAndSubscriptionStatus(UUID userId, String
// subscriptionStatus);

// /**
// * Check if an active subscription exists for a user.
// */
// boolean existsByUserIdAndSubscriptionStatus(UUID userId, String
// subscriptionStatus);

// /**
// * Find subscriptions that are expiring within a certain number of days.
// */
// List<UserSubscription>
// findBySubscriptionEndDateBeforeAndSubscriptionStatus(java.time.LocalDate
// date,
// String subscriptionStatus);

// /**
// * Find all subscriptions with a specific status.
// */
// List<UserSubscription> findBySubscriptionStatus(String subscriptionStatus);
// }
