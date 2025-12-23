package com.team.ja.user.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.ja.common.dto.SkillResponse;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.user.service.SkillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSkillDataConsumer {

    private final SkillService skillService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.ADMIN_REQUEST_SKILL_DATA, groupId = "${spring.kafka.consumer.group-id}")
    @SendTo
    public Message<List<SkillResponse>> handleSkillDataRequest(String message,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) throws Exception {
        log.info("Received skill data request: {}", message);
        List<SkillResponse> skills = objectMapper.convertValue(
                skillService.getAllSkills(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SkillResponse.class));

        log.info("Sending skill data response with {} skills", skills.size());
        return MessageBuilder.withPayload(skills)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();

    }

    @KafkaListener(topics = KafkaTopics.ADMIN_CREATE_SKILL, groupId = "${spring.kafka.consumer.group-id}")
    @SendTo
    public Message<SkillResponse> handleCreateSkillRequest(String skillName,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) throws Exception {
        log.info("Received create skill request for skillName: {}", skillName);
        SkillResponse createdSkill = objectMapper.convertValue(
                skillService.createSkill(skillName),
                SkillResponse.class);

        log.info("Sending create skill response for skillName: {}", skillName);
        return MessageBuilder.withPayload(createdSkill)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
    }

}
