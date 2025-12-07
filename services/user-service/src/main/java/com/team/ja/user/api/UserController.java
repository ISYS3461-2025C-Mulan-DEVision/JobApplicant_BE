package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for User operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the user service is running")
    public ApiResponse<String> health() {
        return ApiResponse.success("User Service is running");
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all active users")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        // return ApiResponse.success(userService.getAllUsers());
        return ApiResponse.success("Fetch data successfully", userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a user by their email")
    public ApiResponse<UserResponse> getUserByEmail(@PathVariable String email) {
        return ApiResponse.success(userService.getUserByEmail(email));
    }
}
