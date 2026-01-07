package com.team.ja.admin.api;

import com.team.ja.admin.client.ApplicationClient;
import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
@Tag(name = "Admin - Application Management", description = "Endpoints for managing job applications")
public class AdminApplicationProxyController {

    private final ApplicationClient applicationClient;

    @GetMapping
    @Operation(summary = "Get all applications", description = "Retrieve all applications with filters")
    public ApiResponse<Object> getAllApplications(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID jobPostId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return applicationClient.getAllApplications(userId, jobPostId, status, page, size);
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by ID", description = "Get detailed information about an application")
    public ApiResponse<Object> getApplicationById(@PathVariable UUID applicationId) {
        return applicationClient.getApplicationById(applicationId);
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update application status", description = "Update status of an application")
    public ApiResponse<Object> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @RequestBody Object request) {
        return applicationClient.updateApplicationStatus(applicationId, request);
    }

    @DeleteMapping("/{applicationId}")
    @Operation(summary = "Delete application", description = "Permanently delete an application")
    public ApiResponse<Void> deleteApplication(@PathVariable UUID applicationId) {
        return applicationClient.deleteApplication(applicationId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get application statistics", description = "Get statistics about applications")
    public ApiResponse<Object> getApplicationStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return applicationClient.getApplicationStatistics(startDate, endDate);
    }

    @PatchMapping("/bulk/status")
    @Operation(summary = "Bulk update status", description = "Update status for multiple applications")
    public ApiResponse<String> bulkUpdateApplicationStatus(@RequestBody Object request) {
        return applicationClient.bulkUpdateApplicationStatus(request);
    }
}
