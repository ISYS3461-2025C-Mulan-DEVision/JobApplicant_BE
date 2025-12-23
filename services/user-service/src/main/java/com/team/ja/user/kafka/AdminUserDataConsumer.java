package com.team.ja.user.kafka;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.ja.common.dto.UserResponse;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserDataConsumer {

        private final UserService userService;
        private final ObjectMapper objectMapper;

        @KafkaListener(topics = KafkaTopics.ADMIN_REQUEST_USER_DATA, groupId = "${spring.kafka.consumer.group-id}")
        @SendTo
        public Message<List<UserResponse>> handleUserDataRequest(String message,
                        @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) throws Exception {
                log.info("Received user data request: {}", message);
                List<UserResponse> users = objectMapper.convertValue(
                                userService.getAllUsers(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponse.class));

                log.info("Sending user data response with {} users", users.size());
                return MessageBuilder.withPayload(users)
                                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                                .build();

        }

        @KafkaListener(topics = KafkaTopics.ADMIN_DEACTIVATE_USER, groupId = "${spring.kafka.consumer.group-id}")
        @SendTo
        public Message<String> handleDeleteUserRequest(UUID userId,
                        @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) throws Exception {
                log.info("Received delete user request for userId: {}", userId);
                userService.deactivateUser(userId);

                log.info("Sending delete user response for userId: {}", userId);
                return MessageBuilder.withPayload("User deactivated successfully.")
                                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                                .build();

        }
}