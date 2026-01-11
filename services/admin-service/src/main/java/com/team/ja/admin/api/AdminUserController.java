package com.team.ja.admin.api;

import com.team.ja.admin.client.UserClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import com.team.ja.common.exception.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Endpoints for managing users")
public class AdminUserController {

    private final UserClient userClient;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (paginated)")
    public ApiResponse<PageResponse<Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userClient.getAllUsers(page, size);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by various criteria")
    public ApiResponse<PageResponse<Object>> searchUsers(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String workExperience,
            @RequestParam(required = false) String employmentTypes,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return userClient.searchUsers(skills, country, city, education, workExperience, employmentTypes, username, page,
                size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user details")
    public ApiResponse<Object> getUserById(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return userClient.getUserById(id);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    public ApiResponse<Void> deactivateUser(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", defaultValue = "") String adminId,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        // In a real scenario, we might want to ensure the caller is an admin here or
        // via Gateway.
        // We pass the adminId header if user-service checks it.
        return userClient.deactivateUser(id, adminId);
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivate a deactivated user account")
    public ApiResponse<Object> reactivateUser(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return userClient.reactivateUser(id);
    }

    private void authorize(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Insufficient permissions for admin endpoint");
        }
    }
}
