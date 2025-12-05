package com.team.ja.subscription.api;

import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription", description = "Subscription and payment endpoints")
public class SubscriptionController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the subscription service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "subscription-service");
        status.put("status", "UP");
        return ApiResponse.success("Subscription Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get subscription service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "subscription-service");
        info.put("version", "0.0.1");
        info.put("description", "Subscription and Payment Management Service");
        info.put("handles", "UserSubscription, UserPaymentTransaction");
        return ApiResponse.success(info);
    }
}

