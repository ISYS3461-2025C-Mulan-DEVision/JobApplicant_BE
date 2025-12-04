package com.team.ja.notification.api;

import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Notification and subscription endpoints")
public class NotificationController {

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
}

