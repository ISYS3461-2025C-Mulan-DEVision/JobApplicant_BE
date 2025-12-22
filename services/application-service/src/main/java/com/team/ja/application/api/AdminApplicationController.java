// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\api\AdminApplicationController.java

package com.team.ja.application.api;

import com.team.ja.application.dto.request.BulkUpdateApplicationStatusRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusAdminRequest;
import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.dto.response.ApplicationStatisticsResponse;
import com.team.ja.application.service.ApplicationService;
import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for admin application endpoints.
 * Handles administrative operations on job applications.
 *
 * Auth: All endpoints require JWT token with admin role.
 */
@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
@Tag(name = "Applications - Admin", description = "Admin job application management endpoints")
public class AdminApplicationController {

    private final ApplicationService applicationService;

    // ==================== ADMIN CRUD ====================

    @GetMapping
    @Operation(summary = "Get all applications", description = "Retrieve all applications in the system with optional filters")
    public ApiResponse<Page<ApplicationResponse>> getAllApplications(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID jobPostId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationResponse> response = applicationService.getAllApplications(userId, jobPostId, status, pageable);
        return ApiResponse.success("Applications retrieved successfully", response);
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by ID", description = "Get detailed information about any application")
    public ApiResponse<ApplicationResponse> getApplicationById(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        ApplicationResponse response = applicationService.getApplicationByIdAdmin(applicationId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update application status", description = "Update application status with admin notes")
    public ApiResponse<ApplicationResponse> updateApplicationStatus(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Valid @RequestBody UpdateApplicationStatusAdminRequest request) {

        ApplicationResponse response = applicationService.updateApplicationStatusAdmin(applicationId, request);
        return ApiResponse.success("Application status updated successfully", response);
    }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete application", description = "Permanently delete an application from the system (hard delete)")
    public ApiResponse<Void> deleteApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        applicationService.deleteApplicationAdmin(applicationId);
        return ApiResponse.success("Application deleted successfully", null);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get application statistics", description = "Get application statistics including total count, by status, and trends")
    public ApiResponse<ApplicationStatisticsResponse> getApplicationStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ApplicationStatisticsResponse response = applicationService.getApplicationStatistics(startDate, endDate);
        return ApiResponse.success("Statistics retrieved successfully", response);
    }

    @PatchMapping("/bulk/status")
    @Operation(summary = "Bulk update application status", description = "Update status for multiple applications at once")
    public ApiResponse<String> bulkUpdateApplicationStatus(
            @Valid @RequestBody BulkUpdateApplicationStatusRequest request) {

        applicationService.bulkUpdateApplicationStatus(request);
        return ApiResponse.success("Bulk update completed successfully", "Updated " + request.getApplicationIds().size() + " applications");
    }

    @PostMapping("/{applicationId}/restore")
    @Operation(summary = "Restore deleted application", description = "Restore a soft-deleted application")
    public ApiResponse<ApplicationResponse> restoreApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        ApplicationResponse response = applicationService.restoreApplication(applicationId);
        return ApiResponse.success("Application restored successfully", response);
    }
}
