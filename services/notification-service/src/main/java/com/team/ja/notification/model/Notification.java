package com.team.ja.notification.model;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.notification.enumeration.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification entity for storing user notifications.
 * Supports different notification types like job matches, application updates, etc.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    /**
     * The ID of the user who receives this notification.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * The type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    /**
     * The title of the notification.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * The detailed message of the notification.
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * The ID of the related job post (if applicable).
     * Stored as String to support external job post IDs from Job Manager.
     */
    @Column(name = "job_post_id")
    private String jobPostId;

    /**
     * The ID of the related application (if applicable).
     */
    @Column(name = "application_id")
    private UUID applicationId;

    /**
     * Whether the notification has been read by the user.
     */
    @lombok.Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /**
     * Timestamp when the notification was read.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Mark the notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark the notification as unread.
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
}

