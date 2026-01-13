// File path: services/admin-service/src/main/java/com/team/ja/admin/client/JobPostAdminClient.java
package com.team.ja.admin.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.JobPostDto;
import com.team.ja.common.dto.jobmanager.JobManagerPageResponse;
import com.team.ja.common.dto.jobmanager.JobSearchRequest;
import com.team.ja.admin.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for Job Manager - Job Post Service (Admin Operations)
 * Service Name: JOB-MANAGER-JOBPOST (registered in Eureka)
 * 
 * This client provides admin-level access to job post management operations.
 */
@FeignClient(name = "${services.jm.jobpost.name}", url = "${services.jm.jobpost.url}", configuration = FeignConfig.class)
public interface JobPostAdminClient {

    /**
     * Search job posts with filters (admin access - all posts)
     * Note: This endpoint returns direct Page data (not wrapped in ApiResponse)
     * 
     * @param request Search criteria including pagination
     * @return Paginated search results (direct Spring Page response, not wrapped)
     */
    @PostMapping("/api/external/job-posts/search")
    JobManagerPageResponse<JobPostDto> searchJobPosts(@RequestBody JobSearchRequest request);

    /**
     * Delete a job post by ID
     * Note: This endpoint returns ApiResponse wrapper
     * 
     * @param id Job post UUID
     * @return ApiResponse with success/failure message
     */
    @DeleteMapping("/api/job-posts/{id}")
    ApiResponse<Void> deleteJobPost(@PathVariable("id") String id);
}
