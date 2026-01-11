package com.team.ja.notification.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.notification.dto.request.CreateNotificationRequest;
import com.team.ja.notification.dto.response.NotificationResponse;
import com.team.ja.notification.dto.response.UnreadCountResponse;
import com.team.ja.notification.enumeration.NotificationType;
import com.team.ja.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for notification operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Notification management endpoints")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the notification service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "notification-service");
        status.put("status", "UP");
        return ApiResponse.success("Notification Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get notification service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "notification-service");
        info.put("version", "0.0.1");
        info.put("description", "Notification and Messaging Service");
        info.put("handles", "UserNotificationSubscription");
        info.put("integrations", "Redis, Kafka, Notification DB");
        return ApiResponse.success(info);
    }

    /**
     * Get all notifications for a user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Get all notifications for a user with pagination")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Notification type filter") @RequestParam(required = false) NotificationType type) {

        log.info("Getting notifications for user: {} page: {} size: {} type: {}", userId, page, size, type);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<NotificationResponse> notifications;
        if (type != null) {
            notifications = notificationService.getUserNotificationsByType(userId, type, pageable);
        } else {
            notifications = notificationService.getUserNotifications(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get a single notification by ID.
     */
    @GetMapping("/{notificationId}/user/{userId}")
    @Operation(summary = "Get notification", description = "Get a single notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        log.info("Getting notification: {} for user: {}", notificationId, userId);
        NotificationResponse notification = notificationService.getNotificationById(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    /**
     * Get unread notification count for a user.
     */
    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread count", description = "Get the count of unread notifications for a user")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        log.info("Getting unread count for user: {}", userId);
        UnreadCountResponse count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    /**
     * Mark a notification as read.
     */
    @PutMapping("/{notificationId}/user/{userId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        log.info("Marking notification: {} as read for user: {}", notificationId, userId);
        NotificationResponse notification = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * Mark all notifications as read for a user.
     */
    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for a user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        log.info("Marking all notifications as read for user: {}", userId);
        int count = notificationService.markAllAsRead(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("markedAsReadCount", count);
        
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", result));
    }

    /**
     * Delete a notification.
     */
    @DeleteMapping("/{notificationId}/user/{userId}")
    @Operation(summary = "Delete notification", description = "Delete (soft delete) a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
            @Parameter(description = "User ID") @PathVariable UUID userId) {

        log.info("Deleting notification: {} for user: {}", notificationId, userId);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    /**
     * Create a notification manually (for admin/testing purposes).
     */
    @PostMapping
    @Operation(summary = "Create notification", description = "Create a notification manually")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        log.info("Creating notification for user: {} of type: {}", request.getUserId(), request.getNotificationType());
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", notification));
    }
}
