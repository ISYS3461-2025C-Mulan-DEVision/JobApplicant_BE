package com.team.ja.user.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.team.ja.common.event.JobPostingEvent;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSearchProfileSkillRepository;
import com.team.ja.user.service.impl.ShardLookupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostMatchConsumer {

    private final ShardLookupService shardLookupService;
    private final UserSearchProfileRepository userSearchProfileRepository;
    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;
    private final UserSearchProfileSkillRepository userSearchProfileSkillRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final List<String> allShards = List.of(
            "user_shard_europe",
            "user_shard_vn",
            "user_shard_sg",
            "user_shard_east_asia",
            "user_shard_oceania",
            "user_shard_north_america",
            "user_shard_others");

    @KafkaListener(topics = KafkaTopics.JOB_POST_PUBLISHED, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "jobPostingEventKafkaListenerContainerFactory")
    public void handleJobPostPublishedEvent(JobPostingEvent event) {
        log.info("Received JobPostPublishedEvent for processing: jobId={}, title={}", event.getJobPostId(), event.getTitle());
        

    }

}
