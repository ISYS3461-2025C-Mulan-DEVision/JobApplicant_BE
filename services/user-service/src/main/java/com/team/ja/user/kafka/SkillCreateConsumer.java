package com.team.ja.user.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SkillCreateEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.model.Skill;
import com.team.ja.user.repository.SkillRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCreateConsumer {

    private final SkillRepository skillRepository;

    private final List<String> allShards = List.of(
            "user_shard_europe",
            "user_shard_vn",
            "user_shard_sg",
            "user_shard_east_asia",
            "user_shard_oceania",
            "user_shard_north_america",
            "user_shard_others"
    // user_shard_others is excluded because it's the source
    );

    @KafkaListener(topics = KafkaTopics.SKILL_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSkillCreate(SkillCreateEvent event) {
        log.info("Received SkillCreateEvent for sync: {}", event.getName());

        for (String shardId : allShards) {
            syncToShard(shardId, event);
        }
    }

    private void syncToShard(String shardId, SkillCreateEvent event) {
        ShardContext.setShardKey(shardId);
        try {
            // Check if it already exists to prevent duplicates/errors
            if (!skillRepository.existsById(event.getSkillId())) {
                Skill skill = Skill.builder()
                        .id(event.getSkillId()) // IMPORTANT: Keep the same ID across all shards
                        .name(event.getName())
                        .normalizedName(event.getNormalizedName())
                        .usageCount(0)
                        .isActive(true)
                        .build();

                skillRepository.save(skill);
                log.info("Successfully synced skill '{}' to shard: {}", event.getName(), shardId);
            }
        } catch (Exception e) {
            log.error("Failed to sync skill to shard {}: {}", shardId, e.getMessage());
        } finally {
            ShardContext.clear();
        }
    }
}
