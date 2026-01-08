package com.team.ja.subscription.api.subscription;

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

import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Management", description = "Create/update/deactivate subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;

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
