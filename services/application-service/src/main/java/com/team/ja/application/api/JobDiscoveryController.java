package com.team.ja.application.api;

import com.team.ja.application.client.CompanyClient;
import com.team.ja.application.client.JobPostClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/discovery")
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
        return jobPostClient.getPublicJobPosts(page, size);
    }

    @PostMapping("/job-posts/search")
    @Operation(summary = "Search for job posts")
    public ApiResponse<JobManagerPageResponse<JobSearchResultDto>> searchJobPosts(
            @RequestBody JobSearchRequest request
    ) {
        return jobPostClient.searchJobPosts(request);
    }

    @GetMapping("/job-posts/{id}")
    @Operation(summary = "Get job post details")
    public ApiResponse<JobPostDto> getJobPostById(@PathVariable UUID id) {
        return jobPostClient.getJobPostById(id);
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Get company details")
    public ApiResponse<CompanyDto> getCompanyById(@PathVariable UUID id) {
        return companyClient.getCompanyById(id);
    }

    @GetMapping("/companies/{id}/profile")
    @Operation(summary = "Get company profile")
    public ApiResponse<CompanyProfileDto> getCompanyProfileById(@PathVariable UUID id) {
        return companyClient.getCompanyProfileById(id);
    }
}
