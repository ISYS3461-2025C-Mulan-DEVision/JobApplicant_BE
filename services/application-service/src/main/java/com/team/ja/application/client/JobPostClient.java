package com.team.ja.application.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.JobManagerPageResponse;
import com.team.ja.common.dto.jobmanager.JobPostDto;
import com.team.ja.common.dto.jobmanager.JobSearchRequest;
import com.team.ja.common.dto.jobmanager.JobSearchResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "${services.jm-job-post.name:JM_JOB_POST}")
public interface JobPostClient {

    @GetMapping("/api/job-posts/public")
    ApiResponse<JobManagerPageResponse<JobPostDto>> getPublicJobPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    @PostMapping("/api/external/job-posts/search")
    ApiResponse<JobManagerPageResponse<JobSearchResultDto>> searchJobPosts(
            @RequestBody JobSearchRequest request
    );
    
    // Internal API if needed
    @GetMapping("/api/job-posts/{id}")
    ApiResponse<JobPostDto> getJobPostById(@PathVariable("id") UUID id);
    
    // External API for basic info might be useful
    @GetMapping("/api/external/job-posts/{id}")
    ApiResponse<JobPostDto> getJobPostBasicInfo(@PathVariable("id") UUID id);
}
