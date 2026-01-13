package com.team.ja.user.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.team.ja.common.event.UserMigrationEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserMigrationDLTConsumer {

    @KafkaListener(topics = "user-migration-event.DLT", groupId = "user-migration-dlt-consumer")
    public void consumeDLT(UserMigrationEvent event) {
        log.error("CRITICAL: User Migration moved to Dead Letter Topic! " +
                "UserId: {}. Manual intervention required.", event.getUserId());
    }
}