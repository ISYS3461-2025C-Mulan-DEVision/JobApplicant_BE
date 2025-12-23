package com.team.ja.admin.kafka.kafka_producer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import com.team.ja.common.dto.SkillResponse;
import com.team.ja.common.dto.UserResponse;
import com.team.ja.common.event.KafkaTopics;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Builder
@RequiredArgsConstructor
public class KafkaRequest {

        private final ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;
        private final ObjectMapper objectMapper;

        /**
         * Request user data from Auth Service via Kafka and wait for the reply.
         * 
         * @param message
         * @return
         * @throws Exception
         */
        public List<UserResponse> sendUserDataRequest(String message) throws Exception {
                ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.ADMIN_REQUEST_USER_DATA,
                                "Get Users");
                record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                                KafkaTopics.ADMIN_REPLY_USER_DATA.getBytes()));

                RequestReplyFuture<String, Object, Object> replyFuture = replyingKafkaTemplate
                                .sendAndReceive(record);
                ConsumerRecord<String, Object> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);

                return objectMapper.convertValue(
                                consumerRecord.value(),
                                new TypeReference<List<UserResponse>>() {
                                });
        }

        /**
         * Send delete user request to User Service via Kafka and wait for the reply.
         * 
         * @param userId
         * @return
         * @throws Exception
         */
        public String deleteUserRequest(UUID userId) throws Exception {
                ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.ADMIN_DEACTIVATE_USER,
                                userId.toString());
                record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                                KafkaTopics.ADMIN_REPLY_DEACTIVATE_USER.getBytes()));

                RequestReplyFuture<String, Object, Object> replyFuture = replyingKafkaTemplate
                                .sendAndReceive(record);
                ConsumerRecord<String, Object> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);

                return (String) consumerRecord.value();
        }

        /**
         * Request skill data from user Service via Kafka and wait for the reply.
         * 
         * @param message
         * @return
         * @throws Exception
         */
        public List<SkillResponse> sendSkillDataRequest(String message) throws Exception {
                ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.ADMIN_REQUEST_SKILL_DATA,
                                "Get Skills");
                record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                                KafkaTopics.ADMIN_REPLY_SKILL_DATA.getBytes()));

                RequestReplyFuture<String, Object, Object> replyFuture = replyingKafkaTemplate
                                .sendAndReceive(record);
                ConsumerRecord<String, Object> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);

                return objectMapper.convertValue(
                                consumerRecord.value(),
                                new TypeReference<List<SkillResponse>>() {
                                });

        }

        /**
         * Create a new skill via Kafka and wait for the reply.
         * 
         * @param skillName
         * @return
         * @throws Exception
         */
        public SkillResponse createSkillRequest(String skillName) throws Exception {
                ProducerRecord<String, Object> record = new ProducerRecord<>(KafkaTopics.ADMIN_CREATE_SKILL,
                                skillName);
                record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC,
                                KafkaTopics.ADMIN_REPLY_CREATE_SKILL.getBytes()));

                RequestReplyFuture<String, Object, Object> replyFuture = replyingKafkaTemplate
                                .sendAndReceive(record);
                ConsumerRecord<String, Object> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);

                return objectMapper.convertValue(
                                consumerRecord.value(),
                                SkillResponse.class);
        }

}
