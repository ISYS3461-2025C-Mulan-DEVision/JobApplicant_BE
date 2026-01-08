package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.model.UserSubscription;

import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse create(CreateSubscriptionRequest request);

    SubscriptionResponse update(UUID id, UpdateSubscriptionRequest request);

    /**
     * Deactivate (soft-delete) a subscription by its ID.
     * Use this method to automatically deactivate subscriptions instead of deleting
     * them.
     */
    void deactivate();

    /**
     * User deactivates (soft-delete) a subscription by its ID.
     * Use this method when a user chooses to deactivate their subscription.
     * This will initiate the deactivation process but may still allow reactivation
     * until the subscription end date.
     * 
     * @param id
     */
    void userDeactivate(UUID id);

    UserSubscription userReactivate(UUID userId, UUID subscriptionId);
}