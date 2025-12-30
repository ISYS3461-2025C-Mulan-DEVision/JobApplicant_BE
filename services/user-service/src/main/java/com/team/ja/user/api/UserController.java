package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.user.dto.request.CreateUserRequest;
import com.team.ja.user.dto.request.UpdateUserRequest;
import com.team.ja.user.dto.response.UserProfileResponse;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for User operations.
 *
 * Auth Integration Notes:
 * - POST /users: Internal endpoint, called by auth-service after registration
 * - PUT /users/{id}: userId should match JWT user (or admin)
 * - DELETE /users/{id}: userId should match JWT user (or admin)
 * - GET endpoints: Public or require authentication based on business rules
 *
 * When integrating with auth-service:
 * 1. Add Spring Security dependency
 * 2. Add @PreAuthorize annotations
 * 3. Replace path variable userId with
 * SecurityContextHolder.getContext().getAuthentication()
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if the user service is running"
    )
    public ApiResponse<String> health() {
        return ApiResponse.success("User Service is running");
    }

    // ==================== USER CRUD ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create user",
        description = "Create a new user profile (called by auth-service after registration)"
    )
    public ApiResponse<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.success(
            "User created successfully",
            userService.createUser(request)
        );
    }

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieve all active users (Admin only in production)"
    )
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(
            "Users retrieved successfully",
            userService.getAllUsers()
        );
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search for users",
        description = "Search for users based on various criteria with pagination"
    )
    public ApiResponse<PageResponse<UserResponse>> searchUsers(
        @RequestParam(required = false) String skills,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<UserResponse> result = userService.searchUsersPaged(
            skills,
            country,
            keyword,
            page,
            size
        );
        return ApiResponse.success("Users retrieved successfully", result);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve a user by their ID"
    )
    public ApiResponse<UserResponse> getUserById(
        @Parameter(description = "User ID") @PathVariable UUID id
    ) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Get user by email",
        description = "Retrieve a user by their email (Internal use)"
    )
    public ApiResponse<UserResponse> getUserByEmail(
        @Parameter(description = "User email") @PathVariable String email
    ) {
        return ApiResponse.success(userService.getUserByEmail(email));
    }

    @GetMapping("/{id}/profile")
    @Operation(
        summary = "Get user profile",
        description = "Retrieve complete user profile with education, experience, and skills"
    )
    public ApiResponse<UserProfileResponse> getUserProfile(
        @Parameter(description = "User ID") @PathVariable UUID id,
        @Parameter(description = "Authenticated User ID") @RequestHeader(
            "X-User-Id"
        ) String authUserId
    ) {
        authorize(id, authUserId);
        return ApiResponse.success(userService.getUserProfile(id));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Update user profile (User can only update own profile)"
    )
    public ApiResponse<UserResponse> updateUser(
        @Parameter(description = "User ID") @PathVariable UUID id,
        @Parameter(description = "Authenticated User ID") @RequestHeader(
            "X-User-Id"
        ) String authUserId,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        authorize(id, authUserId);
        return ApiResponse.success(
            "User updated successfully",
            userService.updateUser(id, request)
        );
    }

    @PostMapping(
        value = "/{id}/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
        summary = "Upload avatar",
        description = "Upload or update a user's avatar"
    )
    public ApiResponse<UserResponse> uploadAvatar(
        @Parameter(description = "User ID") @PathVariable UUID id,
        @Parameter(
            description = "Authenticated User ID from JWT"
        ) @RequestHeader("X-User-Id") String authUserIdStr,
        @Parameter(description = "Avatar image file") @RequestParam(
            "file"
        ) MultipartFile file
    ) {
        authorize(id, authUserIdStr);
        UserResponse response = userService.uploadAvatar(id, file);
        return ApiResponse.success("Avatar uploaded successfully", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Deactivate user",
        description = "Soft delete user account (User can only deactivate own account)"
    )
    public ApiResponse<Void> deactivateUser(
        @Parameter(description = "User ID") @PathVariable UUID id,
        @Parameter(description = "Authenticated User ID") @RequestHeader(
            "X-User-Id"
        ) String authUserId
    ) {
        authorize(id, authUserId);
        userService.deactivateUser(id);
        return ApiResponse.success("User deactivated successfully", null);
    }

    @PostMapping("/{id}/reactivate")
    @Operation(
        summary = "Reactivate user",
        description = "Reactivate a deactivated user (Admin only)"
    )
    public ApiResponse<UserResponse> reactivateUser(
        @Parameter(description = "User ID") @PathVariable UUID id
    ) {
        return ApiResponse.success(
            "User reactivated successfully",
            userService.reactivateUser(id)
        );
    }

    @GetMapping("/exists")
    @Operation(
        summary = "Check email exists",
        description = "Check if email is already registered"
    )
    public ApiResponse<Boolean> existsByEmail(
        @Parameter(description = "Email to check") @RequestParam String email
    ) {
        return ApiResponse.success(userService.existsByEmail(email));
    }

    private void authorize(UUID userIdFromPath, String authUserIdStr) {
        UUID authUserId = UUID.fromString(authUserIdStr);
        if (!userIdFromPath.equals(authUserId)) {
            throw new ForbiddenException(
                "You are not authorized to access this resource."
            );
        }
    }
}
