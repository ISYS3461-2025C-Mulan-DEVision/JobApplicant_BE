package com.team.ja.subscription.api.search_profiles;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.subscription.dto.request.CreateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.response.SearchProfileEmploymentResponse;
import com.team.ja.subscription.service.SearchProfileEmploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription/{userId}/preferences/employments")
@RequiredArgsConstructor
@Tag(name = "SearchProfile Employments", description = "Manage search profile employments")
public class SearchProfileEmploymentController {

    private final SearchProfileEmploymentService searchProfileEmploymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add employment type to search profile")
    public ApiResponse<SearchProfileEmploymentResponse> createEmployment(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody CreateSearchProfileEmploymentRequest request) {
        return ApiResponse.success("Employment added",
                searchProfileEmploymentService.createEmployment(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all employments for user")
    public ApiResponse<List<SearchProfileEmploymentResponse>> getEmployments(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        return ApiResponse.success(searchProfileEmploymentService.getEmploymentsByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employment by id")
    public ApiResponse<SearchProfileEmploymentResponse> getEmploymentById(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Employment record ID") @PathVariable UUID id,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success(searchProfileEmploymentService.getEmploymentById(userId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employment")
    public ApiResponse<SearchProfileEmploymentResponse> updateEmployment(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Employment record ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSearchProfileEmploymentRequest request,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success("Employment updated",
                searchProfileEmploymentService.updateEmployment(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete employment from profile")
    public ApiResponse<Void> deleteEmployment(@Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Employment record ID") @PathVariable UUID id,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        searchProfileEmploymentService.deleteEmployment(userId, id);
        return ApiResponse.success("Employment deleted", null);
    }

    private void authorize(UUID userIdFromPath, String authUserIdStr) {
        UUID authUserId = UUID.fromString(authUserIdStr);
        if (!userIdFromPath.equals(authUserId)) {
            throw new ForbiddenException("You are not authorized to access this resource.");
        }
    }
}
