// File path: services/admin-service/src/main/java/com/team/ja/admin/api/AdminJobPostController.java
package com.team.ja.admin.api;

import com.team.ja.admin.client.JobPostAdminClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.JobPostDto;
import com.team.ja.common.dto.jobmanager.JobManagerPageResponse;
import com.team.ja.common.dto.jobmanager.JobSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing job posts through Job Manager service.
 * Provides admin-level access to search and delete job posts.
 * 
 * Base Path: /api/v1/admin/job-posts
 */
@RestController
@RequestMapping("/api/v1/admin/job-posts")
@RequiredArgsConstructor
@Slf4j
public class AdminJobPostController {

    private final JobPostAdminClient jobPostAdminClient;

    /**
     * Search job posts with filters
     * This is a wrapper endpoint that redirects to Job Manager's search API
     * 
     * @param request Search criteria including title, location, salary, etc.
     * @return Paginated search results wrapped in ApiResponse
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<JobManagerPageResponse<JobPostDto>>> searchJobPosts(@RequestBody JobSearchRequest request) {
        log.info("Admin searching job posts with filters: {}", request);
        
        try {
            // Call Job Manager search endpoint (returns direct Page data, not wrapped)
            JobManagerPageResponse<JobPostDto> result = jobPostAdminClient.searchJobPosts(request);
            
            // Wrap the result in ApiResponse for consistency
            return ResponseEntity.ok(ApiResponse.success(
                "Job posts retrieved successfully",
                result
            ));
        } catch (Exception e) {
            log.error("Failed to search job posts: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.<JobManagerPageResponse<JobPostDto>>error(
                "Failed to search job posts: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete a job post by ID
     * This is a wrapper endpoint that redirects to Job Manager's delete API
     * 
     * @param id Job post UUID
     * @return ApiResponse with success/failure message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPost(@PathVariable String id) {
        log.info("Admin deleting job post with ID: {}", id);
        
        try {
            // Call Job Manager delete endpoint (returns ApiResponse wrapper)
            ApiResponse<Void> response = jobPostAdminClient.deleteJobPost(id);
            
            if (response.isSuccess()) {
                log.info("Job post deleted successfully: {}", id);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to delete job post: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Failed to delete job post {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to delete job post: " + e.getMessage()
            ));
        }
    }
}
