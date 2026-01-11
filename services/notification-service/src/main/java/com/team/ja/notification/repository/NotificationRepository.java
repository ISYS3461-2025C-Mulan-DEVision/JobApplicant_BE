package com.team.ja.notification.repository;

import com.team.ja.notification.enumeration.NotificationType;
import com.team.ja.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all active notifications for a user, ordered by creation date descending.
     */
    Page<Notification> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find all unread notifications for a user.
     */
    List<Notification> findByUserIdAndIsReadFalseAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    /**
     * Find notifications by user and type.
     */
    Page<Notification> findByUserIdAndNotificationTypeAndIsActiveTrueOrderByCreatedAtDesc(
            UUID userId, NotificationType notificationType, Pageable pageable);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndIsReadFalseAndIsActiveTrue(UUID userId);

    /**
     * Find a notification by ID and user ID (for security check).
     */
    Optional<Notification> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false AND n.isActive = true")
    int markAllAsReadByUserId(@Param("userId") UUID userId);

    /**
     * Find notifications by job post ID.
     */
    List<Notification> findByJobPostIdAndIsActiveTrue(String jobPostId);

    /**
     * Find notifications by application ID.
     */
    List<Notification> findByApplicationIdAndIsActiveTrue(UUID applicationId);

    /**
     * Check if a notification already exists for a user and job post (to avoid duplicates).
     */
    boolean existsByUserIdAndJobPostIdAndNotificationTypeAndIsActiveTrue(
            UUID userId, String jobPostId, NotificationType notificationType);
}

