package com.team.ja.auth.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for user registration events.
 * Publishes events when a new user registers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    /**
     * Publish user registered event.
     * 
     * @param event The registration event
     */
    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Publishing user-registered event for userId: {}", event.getUserId());
        try {
            // Wait for acknowledgement synchronously to ensure User Service will eventually get it
            kafkaTemplate.send(KafkaTopics.USER_REGISTERED, event.getUserId().toString(), event).get();
            log.info("User-registered event ACK received for userId: {}", event.getUserId());
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to send user-registered event. Profile creation will fail!", ex);
            throw new RuntimeException("Kafka event failure", ex);
        }
    }
}

