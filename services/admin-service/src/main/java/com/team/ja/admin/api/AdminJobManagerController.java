package com.team.ja.admin.api;

import com.team.ja.admin.client.JobManagerClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import com.team.ja.common.exception.ForbiddenException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Job & Company Management", description = "Endpoints for managing companies and job posts via Job Manager Service")
public class AdminJobManagerController {

    private final JobManagerClient jobManagerClient;

    // ==================== COMPANIES ====================

    @GetMapping("/companies")
    @Operation(summary = "Search companies", description = "Search and list companies")
    public ApiResponse<PageResponse<Object>> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return jobManagerClient.searchCompanies(name, page, size);
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Get company by ID", description = "Retrieve company details")
    public ApiResponse<Object> getCompanyById(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return jobManagerClient.getCompanyById(id);
    }

    @PatchMapping("/companies/{id}/deactivate")
    @Operation(summary = "Deactivate company", description = "Deactivate a company account")
    public ApiResponse<Void> deactivateCompany(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        // Assuming JM has a status update endpoint
        return jobManagerClient.updateCompanyStatus(id, "INACTIVE");
    }

    // ==================== JOB POSTS ====================

    @GetMapping("/job-posts")
    @Operation(summary = "Search job posts", description = "Search and list job posts")
    public ApiResponse<PageResponse<Object>> searchJobPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String companyName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return jobManagerClient.searchJobPosts(title, companyName, page, size);
    }

    @GetMapping("/job-posts/{id}")
    @Operation(summary = "Get job post by ID", description = "Retrieve job post details")
    public ApiResponse<Object> getJobPostById(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return jobManagerClient.getJobPostById(id);
    }

    @DeleteMapping("/job-posts/{id}")
    @Operation(summary = "Delete job post", description = "Permanently delete a job post")
    public ApiResponse<Void> deleteJobPost(@PathVariable UUID id,
            @RequestHeader(value = "X-User-Role") String role) {
        authorize(role);
        return jobManagerClient.deleteJobPost(id);
    }

    private void authorize(String role) {
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Insufficient permissions for admin endpoint");
        }
    }
}
