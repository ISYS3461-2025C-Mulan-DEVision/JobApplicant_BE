package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;

import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse create(CreateSubscriptionRequest request);

    SubscriptionResponse update(UUID id, UpdateSubscriptionRequest request);

    void deactivate(UUID id);
}