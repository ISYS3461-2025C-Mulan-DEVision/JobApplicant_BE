package com.team.ja.subscription.kafka;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.enumeration.PaymentStatus;
import com.team.ja.common.enumeration.SubscriptionStatus;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.subscription.dto.response.PaymentResponse;
import com.team.ja.subscription.model.subscription.UserSubscription;
import com.team.ja.subscription.repository.UserSubscriptionRepository;

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

    private final UserSubscriptionRepository userSubscriptionRepository;

    @KafkaListener(topics = KafkaTopics.APPLICANT_PAYMENT_RESPONSE, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentResponse(PaymentResponse response) {
        log.info("Received payment response: {}", response);
        // Process and update subscription/payment status accordingly
        if (response.getPaymentStatus().equals(PaymentStatus.SUCCEEDED)) {
            log.info("Payment successful, updating subscription status.");
            // Update subscription status to active
            UUID userId = response.getUserId();

            // Find if the user have any pending subscriptions
            List<UserSubscription> pending = userSubscriptionRepository.findByUserIdAndSubscriptionStatus(userId,
                    SubscriptionStatus.PENDING.name());

            // user should have only one pending subscription at most (the logic is for
            // safety)
            if (!pending.isEmpty()) {
                // Activate the first pending subscription and set end date (extend by 30 days)
                UserSubscription subscription = pending.get(0);
                subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

                // Set start date to today if not set
                LocalDate now = LocalDate.now();
                if (subscription.getSubscriptionStartDate() == null) {
                    subscription.setSubscriptionStartDate(now);
                }

                // Extend end date by 30 days from today or from existing end date
                LocalDate base = subscription.getSubscriptionEndDate() != null
                        && subscription.getSubscriptionEndDate().isAfter(now)
                                ? subscription.getSubscriptionEndDate()
                                : now;
                subscription.setSubscriptionEndDate(base.plusDays(30));
                userSubscriptionRepository.save(subscription);
                log.info("Activated subscription id={} for userId={}", subscription.getId(), userId);
                return;
            }

            // If no pending subscription found, try to extend an active subscription
            List<UserSubscription> active = userSubscriptionRepository.findByUserIdAndSubscriptionStatus(userId,
                    SubscriptionStatus.ACTIVE.name());
            if (!active.isEmpty()) {
                UserSubscription sub = active.get(0);
                LocalDate now = LocalDate.now();
                LocalDate base = sub.getSubscriptionEndDate() != null && sub.getSubscriptionEndDate().isAfter(now)
                        ? sub.getSubscriptionEndDate()
                        : now;
                sub.setSubscriptionEndDate(base.plusDays(30));
                userSubscriptionRepository.save(sub);
                log.info("Extended subscription id={} for userId={}", sub.getId(), userId);
                return;
            }

            // If no subscription exists, create a new active subscription record
            UserSubscription newSub = new UserSubscription();
            newSub.setUserId(response.getUserId());
            newSub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            LocalDate start = LocalDate.now();
            newSub.setSubscriptionStartDate(start);
            newSub.setSubscriptionEndDate(start.plusDays(30));
            userSubscriptionRepository.save(newSub);
            log.info("Created and activated new subscription for userId={}", userId);

        } else {
            log.warn("Payment failed, subscription remains inactive.");
            // Handle payment failure scenario

        }
    }

}
