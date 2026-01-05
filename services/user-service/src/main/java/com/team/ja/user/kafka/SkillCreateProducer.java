package com.team.ja.user.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SkillCreateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCreateProducer {

    private final KafkaTemplate<String, SkillCreateEvent> skillCreateKafkaTemplate;

    /**
     * Publishes a SkillCreateEvent.
     *
     * @param event The skill creation event to send.
     */
    public void sendSkillCreateEvent(SkillCreateEvent event) {
        log.info("Publishing skill-created event for skillId: {} and skillName: {}", event.getSkillId(),
                event.getName(), event.getNormalizedName());

        skillCreateKafkaTemplate.send(KafkaTopics.SKILL_CREATED, event.getSkillId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Skill-created event sent successfully for skillId: {} [partition: {}, offset: {}]",
                                event.getSkillId(),
                                event.getName(),
                                event.getNormalizedName(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send skill-created event for skillId: {}", event.getSkillId(), ex);
                        // Depending on requirements, could add to a dead-letter queue or retry
                        // mechanism here.
                    }
                });
    }

}
