package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "User profile and management endpoints")
public class UserController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the user service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "user-service");
        status.put("status", "UP");
        return ApiResponse.success("User Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get user service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "user-service");
        info.put("version", "0.0.1");
        info.put("description", "User Profile and Education Management Service");
        info.put("handles", "User, UserEducation, UserWorkExperience, UserSkill, Country, EmploymentType, EducationLevel, Skill");
        return ApiResponse.success(info);
    }
}

