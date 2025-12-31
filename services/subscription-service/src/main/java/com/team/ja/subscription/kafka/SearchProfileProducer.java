package com.team.ja.subscription.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Producer for sending changes in search profiles.
 * This producer is responsible for publishing events related to search profile
 * changes to
 * jm services that need to be informed about these changes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchProfileProducer {

    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    /**
     * Publishes a UserSearchProfileUpdateEvent.
     *
     * @param event The search profile update event to send.
     */
    public void sendSearchProfileUpdateEvent(UserSearchProfileUpdateEvent event) {
        log.info("Publishing user-search-profile-updated event for userId: {} and type: {}", event.getUserId(),
                event.getUpdateType());

        CompletableFuture<SendResult<String, UserSearchProfileUpdateEvent>> future = userSearchProfileUpdateKafkaTemplate
                .send(KafkaTopics.USER_SEARCH_PROFILE_UPDATED,
                        event.getUserId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "User-search-profile-updated event sent successfully for userId: {} [partition: {}, offset: {}]",
                        event.getUserId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user-search-profile-updated event for userId: {}", event.getUserId(), ex);
            }
        });
    }
}