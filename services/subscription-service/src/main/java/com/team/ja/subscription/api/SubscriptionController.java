package com.team.ja.subscription.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.dto.request.CreateSubscriptionRequest;
import com.team.ja.subscription.dto.request.UpdateSubscriptionRequest;
import com.team.ja.subscription.dto.response.SubscriptionResponse;
import com.team.ja.subscription.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Management", description = "Create/update/deactivate subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

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

    @PostMapping
    @Operation(summary = "Create subscription")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.create(request);
        return ApiResponse.success("Subscription created", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subscription")
    public ApiResponse<SubscriptionResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        SubscriptionResponse resp = subscriptionService.update(id, request);
        return ApiResponse.success("Subscription updated", resp);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate (soft-delete) subscription")
    public void deactivate(@PathVariable UUID id) {
        subscriptionService.userDeactivate(id);
    }

    @PostMapping("/{id}/{subscriptionId}")
    public ApiResponse<SubscriptionResponse> reactivate(@PathVariable UUID id, @PathVariable UUID subscriptionId) {
        subscriptionService.userReactivate(id, subscriptionId);
        return ApiResponse.success("Subscription reactivated");
    }
}
