package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.subscription.UserSubscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    List<UserSubscription> findByUserId(UUID userId);

    List<UserSubscription> findByUserIdAndIsActiveTrue(UUID userId);
}
