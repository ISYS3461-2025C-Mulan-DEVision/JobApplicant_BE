package com.team.ja.application.api;

import com.team.ja.application.dto.request.CreateApplicationRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusRequest;
import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.service.ApplicationService;
import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for public application endpoints (Applicant access).
 * Handles CRUD operations for job applications submitted by users.
 *
 * Auth: All endpoints require JWT token with authenticated user context.
 * The userId is extracted from the JWT token in production.
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications - Public", description = "Public job application endpoints for applicants")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ==================== HEALTH CHECKS ====================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the application service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "application-service");
        status.put("status", "UP");
        return ApiResponse.success("Application Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get application service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "application-service");
        info.put("version", "0.0.1");
        info.put("description", "Job Application Management Service");
        return ApiResponse.success(info);
    }

    // ==================== APPLICATION CRUD ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create job application", description = "Submit a new job application with resume and optional files")
    public ApiResponse<ApplicationResponse> createApplication(
            @RequestHeader("X-User-ID") String userId,
            @Valid CreateApplicationRequest request) {
        UUID userUUID = UUID.fromString(userId);
        ApplicationResponse response = applicationService.createApplication(userUUID, request);
        return ApiResponse.success("Application submitted successfully", response);
    }

    @GetMapping
    @Operation(summary = "Get user's applications", description = "Retrieve all applications submitted by authenticated user")
    public ApiResponse<Page<ApplicationResponse>> getUserApplications(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        UUID userUUID = UUID.fromString(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationResponse> response = applicationService.getUserApplications(userUUID, pageable);
        return ApiResponse.success("Applications retrieved successfully", response);
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application by ID", description = "Get detailed information about a specific application")
    public ApiResponse<ApplicationResponse> getApplicationById(
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        UUID userUUID = UUID.fromString(userId);
        ApplicationResponse response = applicationService.getApplicationById(userUUID, applicationId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update application status", description = "Update application status and optionally add notes")
    public ApiResponse<ApplicationResponse> updateApplicationStatus(
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {

        UUID userUUID = UUID.fromString(userId);
        ApplicationResponse response = applicationService.updateApplicationStatus(userUUID, applicationId, request);
        return ApiResponse.success("Application status updated successfully", response);
    }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Withdraw application", description = "Soft delete (withdraw) a job application")
    public ApiResponse<Void> withdrawApplication(
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        UUID userUUID = UUID.fromString(userId);
        applicationService.withdrawApplication(userUUID, applicationId);
        return ApiResponse.success("Application withdrawn successfully", null);
    }

    @GetMapping("/{applicationId}/files/{fileType}")
    @Operation(summary = "Download application file", description = "Get download URL for resume, cover letter, or additional files")
    public ApiResponse<String> getApplicationFile(
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "File type: resume, coverLetter, additional_0, etc") @PathVariable String fileType) {

        UUID userUUID = UUID.fromString(userId);
        String fileUrl = applicationService.getApplicationFileUrl(userUUID, applicationId, fileType);
        return ApiResponse.success("File URL retrieved successfully", fileUrl);
    }
}

