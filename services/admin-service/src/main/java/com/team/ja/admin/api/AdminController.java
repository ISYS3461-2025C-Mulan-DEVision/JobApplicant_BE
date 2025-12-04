package com.team.ja.admin.api;

import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrator management endpoints")
public class AdminController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the admin service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "admin-service");
        status.put("status", "UP");
        return ApiResponse.success("Admin Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get admin service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "admin-service");
        info.put("version", "0.0.1");
        info.put("description", "Administrator Management Service");
        info.put("handles", "Administrator");
        return ApiResponse.success(info);
    }
}

