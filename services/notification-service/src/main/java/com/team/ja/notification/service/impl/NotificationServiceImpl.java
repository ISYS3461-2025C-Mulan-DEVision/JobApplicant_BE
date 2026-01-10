package com.team.ja.notification.service.impl;

import com.team.ja.notification.dto.request.CreateNotificationRequest;
import com.team.ja.notification.dto.response.NotificationResponse;
import com.team.ja.notification.dto.response.UnreadCountResponse;
import com.team.ja.notification.enumeration.NotificationType;
import com.team.ja.notification.model.Notification;
import com.team.ja.notification.repository.NotificationRepository;
import com.team.ja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of NotificationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {} of type: {}", request.getUserId(), request.getNotificationType());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .message(request.getMessage())
                .jobPostId(request.getJobPostId())
                .applicationId(request.getApplicationId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotificationsByType(UUID userId, NotificationType notificationType, Pageable pageable) {
        log.debug("Fetching notifications for user: {} of type: {}", userId, notificationType);
        return notificationRepository.findByUserIdAndNotificationTypeAndIsActiveTrueOrderByCreatedAtDesc(userId, notificationType, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId, UUID userId) {
        log.debug("Fetching notification: {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        log.info("Marking notification: {} as read for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        
        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        log.info("Deleting notification: {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findByIdAndUserIdAndIsActiveTrue(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        
        notification.deactivate();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        log.debug("Getting unread count for user: {}", userId);
        long count = notificationRepository.countByUserIdAndIsReadFalseAndIsActiveTrue(userId);
        return UnreadCountResponse.builder()
                .userId(userId)
                .unreadCount(count)
                .build();
    }

    @Override
    @Transactional
    public void createJobMatchNotification(UUID userId, String jobPostId, String jobTitle) {
        log.info("Creating job match notification for user: {} for job: {}", userId, jobPostId);

        // Check if notification already exists to avoid duplicates
        if (notificationRepository.existsByUserIdAndJobPostIdAndNotificationTypeAndIsActiveTrue(
                userId, jobPostId, NotificationType.JOB_MATCH)) {
            log.info("Job match notification already exists for user: {} and job: {}", userId, jobPostId);
            return;
        }

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .notificationType(NotificationType.JOB_MATCH)
                .title("New Job Match!")
                .message("A new job posting matches your search profile: " + (jobTitle != null ? jobTitle : "Check it out!"))
                .jobPostId(jobPostId)
                .build();

        createNotification(request);
    }

    @Override
    @Transactional
    public void createApplicationSubmittedNotification(UUID userId, UUID applicationId, String jobPostId) {
        log.info("Creating application submitted notification for user: {} for application: {}", userId, applicationId);

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .notificationType(NotificationType.APPLICATION_SUBMITTED)
                .title("Application Submitted")
                .message("Your application has been successfully submitted.")
                .applicationId(applicationId)
                .jobPostId(jobPostId)
                .build();

        createNotification(request);
    }

    /**
     * Map Notification entity to NotificationResponse DTO.
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .jobPostId(notification.getJobPostId())
                .applicationId(notification.getApplicationId())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

