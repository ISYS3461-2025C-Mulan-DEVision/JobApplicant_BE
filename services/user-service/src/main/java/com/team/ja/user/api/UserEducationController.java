package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.user.dto.request.CreateUserEducationRequest;
import com.team.ja.user.dto.request.UpdateUserEducationRequest;
import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.service.UserEducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for User Education operations.
 * 
 * Auth Integration Notes:
 * - All operations require authenticated user
 * - User can only manage their own education records
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/education")
@RequiredArgsConstructor
@Tag(name = "User Education", description = "User education management endpoints")
public class UserEducationController {

    private final UserEducationService userEducationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add education", description = "Add a new education entry")
    public ApiResponse<UserEducationResponse> createEducation(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
            @Valid @RequestBody CreateUserEducationRequest request) {
        authorize(userId, authUserId);
        return ApiResponse.success(
                "Education added successfully",
                userEducationService.createEducation(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all education", description = "Get all education entries for a user")
    public ApiResponse<List<UserEducationResponse>> getEducation(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success(userEducationService.getEducationByUserId(userId));
    }

    @GetMapping("/{educationId}")
    @Operation(summary = "Get education by ID", description = "Get a specific education entry")
    public ApiResponse<UserEducationResponse> getEducationById(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Education ID") @PathVariable UUID educationId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success(userEducationService.getEducationById(userId, educationId));
    }

    @PutMapping("/{educationId}")
    @Operation(summary = "Update education", description = "Update an education entry")
    public ApiResponse<UserEducationResponse> updateEducation(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Education ID") @PathVariable UUID educationId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
            @Valid @RequestBody UpdateUserEducationRequest request) {
        authorize(userId, authUserId);
        return ApiResponse.success(
                "Education updated successfully",
                userEducationService.updateEducation(userId, educationId, request));
    }

    @DeleteMapping("/{educationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete education", description = "Delete an education entry")
    public ApiResponse<Void> deleteEducation(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Education ID") @PathVariable UUID educationId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        userEducationService.deleteEducation(userId, educationId);
        return ApiResponse.success("Education deleted successfully", null);
    }

    private void authorize(UUID userIdFromPath, String authUserIdStr) {
        UUID authUserId = UUID.fromString(authUserIdStr);
        if (!userIdFromPath.equals(authUserId)) {
            throw new ForbiddenException("You are not authorized to access this resource.");
        }
    }
}

