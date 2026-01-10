package com.team.ja.notification.service;

import com.team.ja.notification.dto.request.CreateNotificationRequest;
import com.team.ja.notification.dto.response.NotificationResponse;
import com.team.ja.notification.dto.response.UnreadCountResponse;
import com.team.ja.notification.enumeration.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for notification operations.
 */
public interface NotificationService {

    /**
     * Create a new notification.
     *
     * @param request the notification creation request
     * @return the created notification response
     */
    NotificationResponse createNotification(CreateNotificationRequest request);

    /**
     * Get all notifications for a user with pagination.
     *
     * @param userId   the user ID
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);

    /**
     * Get notifications for a user filtered by type.
     *
     * @param userId           the user ID
     * @param notificationType the notification type filter
     * @param pageable         pagination parameters
     * @return page of notifications
     */
    Page<NotificationResponse> getUserNotificationsByType(UUID userId, NotificationType notificationType, Pageable pageable);

    /**
     * Get a single notification by ID.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for authorization check)
     * @return the notification response
     */
    NotificationResponse getNotificationById(UUID notificationId, UUID userId);

    /**
     * Mark a notification as read.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for authorization check)
     * @return the updated notification response
     */
    NotificationResponse markAsRead(UUID notificationId, UUID userId);

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId the user ID
     * @return the number of notifications marked as read
     */
    int markAllAsRead(UUID userId);

    /**
     * Delete (soft delete) a notification.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID (for authorization check)
     */
    void deleteNotification(UUID notificationId, UUID userId);

    /**
     * Get the count of unread notifications for a user.
     *
     * @param userId the user ID
     * @return the unread count response
     */
    UnreadCountResponse getUnreadCount(UUID userId);

    /**
     * Create a job match notification.
     *
     * @param userId    the user ID
     * @param jobPostId the matched job post ID
     * @param jobTitle  the job title
     */
    void createJobMatchNotification(UUID userId, String jobPostId, String jobTitle);

    /**
     * Create an application submitted notification.
     *
     * @param userId        the user ID
     * @param applicationId the application ID
     * @param jobPostId     the job post ID
     */
    void createApplicationSubmittedNotification(UUID userId, UUID applicationId, String jobPostId);
}

