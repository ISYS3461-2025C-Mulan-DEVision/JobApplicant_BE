package com.team.ja.user.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.model.User;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.impl.ShardLookupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubscriptionConsumer {

    private final ShardLookupService shardLookupService;
    private final UserRepository userRepository;

    @KafkaListener(topics = KafkaTopics.SUBSCRIPTION_ACTIVATE, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSubscriptionActivateEvent(SubscriptionActivateEvent event) {
        log.info("Received subscription activate event for user subscription processing: {}", event);

        try {

            // Determine the shard for the user
            String shardKey = shardLookupService.findShardIdByUserId(event.getPayerId());
            ShardContext.setShardKey(shardKey);

            // Update user subscription status in user profile
            User user = userRepository.findById(event.getPayerId()).orElseThrow(() -> {
                log.warn("User with ID: {} not found for subscription update", event.getPayerId());
                return new IllegalArgumentException("User not found");
            });
            user.setPremium(true);
            userRepository.save(user);
            log.info("Updated user subscription status for user ID: {}", event.getPayerId());
        } catch (Exception e) {
            log.error("Error processing subscription activate event for user ID: {}", event.getPayerId(), e);
        } finally {
            ShardContext.clear();
        }
    }

    @KafkaListener(topics = KafkaTopics.SUBSCRIPTION_DEACTIVATE, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSubscriptionDeactivateEvent(SubscriptionDeactivateEvent event) {
        log.info("Received subscription deactivate event for user subscription processing: {}", event);

        try {
            // Determine the shard for the user
            String shardKey = shardLookupService.findShardIdByUserId(event.getPayerId());
            ShardContext.setShardKey(shardKey);

            // Update user subscription status in user profile
            User user = userRepository.findById(event.getPayerId()).orElseThrow(() -> {
                log.warn("User with ID: {} not found for subscription update", event.getPayerId());
                return new IllegalArgumentException("User not found");
            });
            user.setPremium(false);
            userRepository.save(user);
            log.info("Updated user subscription status for user ID: {}", event.getPayerId());
        } finally {
            ShardContext.clear();
        }
    }

}
