package com.team.ja.notification.dto.response;

import com.team.ja.notification.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a single notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private String jobPostId;
    private UUID applicationId;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

