package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.user.dto.request.CreateUserWorkExperienceRequest;
import com.team.ja.user.dto.request.UpdateUserWorkExperienceRequest;
import com.team.ja.user.dto.response.UserWorkExperienceResponse;
import com.team.ja.user.service.UserWorkExperienceService;
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
 * REST controller for User Work Experience operations.
 * 
 * Auth Integration Notes:
 * - All operations require authenticated user
 * - User can only manage their own work experience records
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/work-experience")
@RequiredArgsConstructor
@Tag(name = "User Work Experience", description = "User work experience management endpoints")
public class UserWorkExperienceController {

    private final UserWorkExperienceService workExperienceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add work experience", description = "Add a new work experience entry")
    public ApiResponse<UserWorkExperienceResponse> createWorkExperience(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
            @Valid @RequestBody CreateUserWorkExperienceRequest request) {
        authorize(userId, authUserId);
        return ApiResponse.success(
                "Work experience added successfully",
                workExperienceService.createWorkExperience(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all work experience", description = "Get all work experience entries for a user")
    public ApiResponse<List<UserWorkExperienceResponse>> getWorkExperience(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success(workExperienceService.getWorkExperienceByUserId(userId));
    }

    @GetMapping("/{workExpId}")
    @Operation(summary = "Get work experience by ID", description = "Get a specific work experience entry")
    public ApiResponse<UserWorkExperienceResponse> getWorkExperienceById(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Work Experience ID") @PathVariable UUID workExpId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        return ApiResponse.success(workExperienceService.getWorkExperienceById(userId, workExpId));
    }

    @PutMapping("/{workExpId}")
    @Operation(summary = "Update work experience", description = "Update a work experience entry")
    public ApiResponse<UserWorkExperienceResponse> updateWorkExperience(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Work Experience ID") @PathVariable UUID workExpId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
            @Valid @RequestBody UpdateUserWorkExperienceRequest request) {
        authorize(userId, authUserId);
        return ApiResponse.success(
                "Work experience updated successfully",
                workExperienceService.updateWorkExperience(userId, workExpId, request));
    }

    @DeleteMapping("/{workExpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete work experience", description = "Delete a work experience entry")
    public ApiResponse<Void> deleteWorkExperience(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Work Experience ID") @PathVariable UUID workExpId,
            @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
        authorize(userId, authUserId);
        workExperienceService.deleteWorkExperience(userId, workExpId);
        return ApiResponse.success("Work experience deleted successfully", null);
    }

    private void authorize(UUID userIdFromPath, String authUserIdStr) {
        UUID authUserId = UUID.fromString(authUserIdStr);
        if (!userIdFromPath.equals(authUserId)) {
            throw new ForbiddenException("You are not authorized to access this resource.");
        }
    }
}

