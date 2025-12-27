package com.team.ja.subscription.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.mapper.SubscriptionMapper;
import com.team.ja.subscription.model.subscription.UserSubscription;
import com.team.ja.subscription.repository.SubscriptionRepository;
import com.team.ja.common.enumeration.SubscriptionStatus;
import com.team.ja.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        // Always create a new subscription record. Do NOT reactivate old records.
        UserSubscription entity = subscriptionMapper.toEntity(request);
        if (entity.getSubscriptionStatus() == null) {
            entity.setSubscriptionStatus(SubscriptionStatus.PENDING);
        }

        // default dates: start = today, end = 30 days from today
        if (entity.getSubscriptionStartDate() == null) {
            entity.setSubscriptionStartDate(LocalDate.now());
        }
        if (entity.getSubscriptionEndDate() == null) {
            entity.setSubscriptionEndDate(LocalDate.now().plusDays(30));
        }
        UserSubscription saved = subscriptionRepository.save(entity);
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

    @Override
    @Transactional
    public void deactivate(UUID id) {
        UserSubscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription", "id", id.toString()));
        existing.deactivate();
        subscriptionRepository.save(existing);
    }
}
