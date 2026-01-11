package com.team.ja.auth.kafka;

import java.util.concurrent.Flow.Subscription;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.auth.repository.AuthCredentialRepository;
import com.team.ja.common.enumeration.Role;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SubscriptionActivateEvent;
import com.team.ja.common.event.SubscriptionDeactivateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleConsumer {

    private final AuthCredentialRepository authCredentialRepository;

    @KafkaListener(topics = KafkaTopics.SUBSCRIPTION_ACTIVATE, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSubscriptionActivateEvent(SubscriptionActivateEvent event) {
        log.info("Received subscription activate event for user ID: {}", event.getPayerId());

        authCredentialRepository.findByUserIdAndIsActiveTrue(event.getPayerId()).ifPresent(credential -> {
            credential.setRole(Role.PREMIMUM);
            authCredentialRepository.save(credential);
            log.info("Updated user role to PREMIUM for user ID: {}", event.getPayerId());
        });
    }

    @KafkaListener(topics = KafkaTopics.SUBSCRIPTION_DEACTIVATE, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSubscriptionDeactivateEvent(SubscriptionDeactivateEvent event) {
        log.info("Received subscription deactivate event for user ID: {}", event.getPayerId());

        authCredentialRepository.findByUserIdAndIsActiveTrue(event.getPayerId()).ifPresent(credential -> {
            credential.setRole(Role.FREE);
            authCredentialRepository.save(credential);
            log.info("Updated user role to FREE for user ID: {}", event.getPayerId());
        });
    }

}
