package com.team.ja.admin.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "Aggregated system statistics")
public class AdminDashboardController {

    // Inject clients here to fetch real stats
    // private final UserClient userClient;
    // private final ApplicationClient applicationClient;

    @GetMapping("/stats")
    @Operation(summary = "Get system stats", description = "Aggregated statistics for the admin dashboard")
    public ApiResponse<Map<String, Object>> getSystemStats(@RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        // Placeholder for aggregated stats
        // In a real implementation, call userClient.count(), applicationClient.count(),
        // etc.
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", "Fetching...");
        stats.put("totalApplications", "Fetching...");
        stats.put("activeJobs", "Fetching..."); // Would come from Job Manager

        return ApiResponse.success(stats);
    }

    private void authorize(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Insufficient permissions for admin endpoint");
        }
    }
}
