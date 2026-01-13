package com.team.ja.user.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.user.service.impl.UserMigrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMigrateConsumer {

    private final UserMigrationService userMigrationService;

    /**
     * Handles incoming user migration events.
     *
     * @param event The user migration event containing migration details.
     */
    @KafkaListener(topics = KafkaTopics.USER_MIGRATION, groupId = "user-migration-consumer")
    public void handleUserMigration(com.team.ja.common.event.UserMigrationEvent event) {
        log.info("Received user migration event for userId: {}", event.getUserId());
        try {
            userMigrationService.migrateUserData(event);
            log.info("User data migration completed for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to migrate user data for userId: {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
