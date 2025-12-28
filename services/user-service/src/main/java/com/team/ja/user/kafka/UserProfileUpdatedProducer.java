package com.team.ja.user.kafka;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for user profile update events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdatedProducer {

    private final KafkaTemplate<String, UserProfileUpdatedEvent> userProfileUpdatedKafkaTemplate;

    /**
     * Publishes a UserProfileUpdatedEvent.
     *
     * @param event The profile update event to send.
     */
    public void sendProfileUpdatedEvent(UserProfileUpdatedEvent event) {
        log.info("Publishing user-profile-updated event for userId: {} and type: {}", event.getUserId(), event.getUpdateType());

        CompletableFuture<SendResult<String, UserProfileUpdatedEvent>> future =
                userProfileUpdatedKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATED, event.getUserId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User-profile-updated event sent successfully for userId: {} [partition: {}, offset: {}]",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user-profile-updated event for userId: {}", event.getUserId(), ex);
                // Depending on requirements, could add to a dead-letter queue or retry mechanism here.
            }
        });
    }
}
