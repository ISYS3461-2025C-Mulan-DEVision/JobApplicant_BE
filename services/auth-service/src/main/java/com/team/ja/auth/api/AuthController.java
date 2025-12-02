package com.team.ja.auth.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the auth service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "auth-service");
        status.put("status", "UP");
        return ApiResponse.success("Auth Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get auth service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "auth-service");
        info.put("version", "0.0.1");
        info.put("description", "Authentication and User Management Service");
        return ApiResponse.success(info);
    }

    // Example endpoint demonstrating exception handling
    @GetMapping("/demo/user/{id}")
    @Operation(summary = "Demo: Get user by ID", description = "Demonstrates exception handling - throws NotFoundException for id > 100")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ApiResponse<Map<String, Object>> getDemoUser(
            @Parameter(description = "User ID (use > 100 to trigger 404)") 
            @PathVariable Long id) {
        
        // Demo: Throw NotFoundException for IDs > 100
        if (id > 100) {
            throw new NotFoundException("User", id);
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("email", "user" + id + "@example.com");
        user.put("name", "Demo User " + id);
        return ApiResponse.success(user);
    }
}
