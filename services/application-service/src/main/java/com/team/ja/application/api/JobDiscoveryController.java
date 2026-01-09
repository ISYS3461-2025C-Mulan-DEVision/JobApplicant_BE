package com.team.ja.application.api;

import com.team.ja.application.client.CompanyClient;
import com.team.ja.application.client.JobPostClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/api/applications/discovery")
@RequiredArgsConstructor
@Tag(name = "Job Discovery", description = "Endpoints for searching jobs and viewing companies (Proxy to Job Manager)")
public class JobDiscoveryController {

    private final JobPostClient jobPostClient;
    private final CompanyClient companyClient;

    @GetMapping("/job-posts")
    @Operation(summary = "Get list of public job posts")
    public ApiResponse<JobManagerPageResponse<JobPostDto>> getPublicJobPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        log.info("Fetching public job posts: page={}, size={}", page, size);
        JobManagerPageResponse<JobPostDto> data = jobPostClient.getPublicJobPosts(page, size);
        log.info("Retrieved {} job posts", data.getTotalElements());
        return ApiResponse.success("Job posts retrieved successfully", data);
    }

    @PostMapping("/job-posts/search")
    @Operation(summary = "Search for job posts")
    public ApiResponse<JobManagerPageResponse<JobSearchResultDto>> searchJobPosts(
            @RequestBody JobSearchRequest request
    ) {
        log.info("Search request received: {}", request);
        JobManagerPageResponse<JobSearchResultDto> data = jobPostClient.searchJobPosts(request);
        log.info("Search returned {} results out of {} total", data.getContent().size(), data.getTotalElements());
        return ApiResponse.success("Job posts search completed", data);
    }

    @GetMapping("/job-posts/{id}")
    @Operation(summary = "Get job post details")
    public ApiResponse<JobPostDto> getJobPostById(@PathVariable UUID id) {
        log.info("Fetching job post: {}", id);
        ApiResponse<JobPostDto> jmResponse = jobPostClient.getJobPostById(id);
        if (jmResponse.isSuccess() && jmResponse.getData() != null) {
            return ApiResponse.success("Job post retrieved successfully", jmResponse.getData());
        }
        return ApiResponse.error(jmResponse.getMessage() != null ? jmResponse.getMessage() : "Job post not found");
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Get company details")
    public ApiResponse<CompanyDto> getCompanyById(@PathVariable UUID id) {
        log.info("Fetching company: {}", id);
        ApiResponse<CompanyDto> jmResponse = companyClient.getCompanyById(id);
        if (jmResponse.isSuccess() && jmResponse.getData() != null) {
            return ApiResponse.success("Company retrieved successfully", jmResponse.getData());
        }
        return ApiResponse.error(jmResponse.getMessage() != null ? jmResponse.getMessage() : "Company not found");
    }

    @GetMapping("/companies/{id}/profile")
    @Operation(summary = "Get company profile")
    public ApiResponse<CompanyProfileDto> getCompanyProfileById(@PathVariable UUID id) {
        log.info("Fetching company profile: {}", id);
        ApiResponse<CompanyProfileDto> jmResponse = companyClient.getCompanyProfileById(id);
        if (jmResponse.isSuccess() && jmResponse.getData() != null) {
            return ApiResponse.success("Company profile retrieved successfully", jmResponse.getData());
        }
        return ApiResponse.error(jmResponse.getMessage() != null ? jmResponse.getMessage() : "Company profile not found");
    }
}
