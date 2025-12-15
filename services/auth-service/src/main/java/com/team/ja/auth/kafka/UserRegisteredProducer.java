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
        
        CompletableFuture<SendResult<String, UserRegisteredEvent>> future = 
                kafkaTemplate.send(KafkaTopics.USER_REGISTERED, event.getUserId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User-registered event sent successfully for userId: {} [partition: {}, offset: {}]",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user-registered event for userId: {}", event.getUserId(), ex);
            }
        });
    }
}

