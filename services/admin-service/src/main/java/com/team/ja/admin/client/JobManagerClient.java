package com.team.ja.admin.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
//DEPRECATED: use AdminCompanyController and AdminJobPostController.
/**
 * Feign client for Job Manager Service.
 * Assumes standard REST endpoints for Companies and Job Posts.
 */
@FeignClient(name = "job-manager-service", url = "${application.job-manager-service.url:}") // Fallback URL if service discovery fails or for local dev
public interface JobManagerClient {

    // ==================== COMPANY ENDPOINTS ====================

    @GetMapping("/api/v1/companies")
    ApiResponse<PageResponse<Object>> searchCompanies(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size);

    @GetMapping("/api/v1/companies/{id}")
    ApiResponse<Object> getCompanyById(@PathVariable("id") UUID id);

    @PatchMapping("/api/v1/companies/{id}/status")
    ApiResponse<Void> updateCompanyStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status); // E.g., ACTIVE, INACTIVE

    // ==================== JOB POST ENDPOINTS ====================

    @GetMapping("/api/v1/job-posts")
    ApiResponse<PageResponse<Object>> searchJobPosts(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size);

    @GetMapping("/api/v1/job-posts/{id}")
    ApiResponse<Object> getJobPostById(@PathVariable("id") UUID id);

    @DeleteMapping("/api/v1/job-posts/{id}")
    ApiResponse<Void> deleteJobPost(@PathVariable("id") UUID id);
}
