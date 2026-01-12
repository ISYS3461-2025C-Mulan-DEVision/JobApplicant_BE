package com.team.ja.admin.api;

import com.team.ja.admin.client.ApplicationClient;
import com.team.ja.admin.client.JobPostAdminClient;
import com.team.ja.admin.client.UserClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.JobSearchRequest;
import com.team.ja.common.exception.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Aggregated system statistics")
public class AdminDashboardController {

    private final UserClient userClient;
    private final ApplicationClient applicationClient;
    private final JobPostAdminClient jobPostAdminClient;

    @GetMapping("/stats")
    @Operation(summary = "Get system stats", description = "Aggregated statistics for the admin dashboard")
    public ApiResponse<Map<String, Object>> getSystemStats(@RequestHeader(value = "X-User-Role") String role) {
        authorize(role);

        // 1. Fetch Total Users
        CompletableFuture<Long> usersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // Request page 0 size 1 just to get the totalElements metadata
                var response = userClient.getAllUsers(0, 1);
                if (response.isSuccess() && response.getData() != null) {
                    return response.getData().getTotalElements();
                }
                return 0L;
            } catch (Exception e) {
                log.error("Failed to fetch user stats", e);
                return 0L;
            }
        });

        // 2. Fetch Total Applications
        CompletableFuture<Long> applicationsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                var response = applicationClient.getApplicationStatistics(null, null);
                if (response.isSuccess() && response.getData() != null) {
                    // Response data is generic Object (likely LinkedHashMap), extract field safely
                    return extractLongFromMap(response.getData(), "totalApplications");
                }
                return 0L;
            } catch (Exception e) {
                log.error("Failed to fetch application stats", e);
                return 0L;
            }
        });

        // 3. Fetch Active Jobs
        CompletableFuture<Long> jobsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // Use empty search request to get total count of active jobs
                var response = jobPostAdminClient.searchJobPosts(JobSearchRequest.builder().build());
                return (long) response.getTotalElements();
            } catch (Exception e) {
                log.error("Failed to fetch job post stats", e);
                return 0L;
            }
        });

        // Wait for all
        CompletableFuture.allOf(usersFuture, applicationsFuture, jobsFuture).join();

        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalUsers", usersFuture.get());
            stats.put("totalApplications", applicationsFuture.get());
            stats.put("activeJobs", jobsFuture.get());
        } catch (Exception e) {
            log.error("Error assembling dashboard stats", e);
            throw new RuntimeException("Failed to aggregate dashboard statistics");
        }

        return ApiResponse.success(stats);
    }

    private void authorize(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Insufficient permissions for admin endpoint");
        }
    }

    private Long extractLongFromMap(Object data, String key) {
        if (data instanceof Map) {
            Object val = ((Map<?, ?>) data).get(key);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            }
        }
        return 0L;
    }
}
