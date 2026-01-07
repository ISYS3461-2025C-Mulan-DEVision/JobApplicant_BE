// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\service\ApplicationService.java

package com.team.ja.application.service;

import com.team.ja.application.dto.request.BulkUpdateApplicationStatusRequest;
import com.team.ja.application.dto.request.CreateApplicationRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusAdminRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusRequest;
import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.dto.response.ApplicationStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for Application operations.
 * Defines business logic for job applications.
 */
public interface ApplicationService {

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Create a new job application with file uploads.
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param request Application creation request with files
     * @return Created application response
     */
    ApplicationResponse createApplication(UUID userId, CreateApplicationRequest request);

    /**
     * Get all applications submitted by the user.
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param pageable Pagination parameters
     * @return Page of user's applications
     */
    Page<ApplicationResponse> getUserApplications(UUID userId, Pageable pageable);

    /**
     * Get a specific application by ID (user can only access their own).
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param applicationId Application ID
     * @return Application response
     */
    ApplicationResponse getApplicationById(UUID userId, UUID applicationId);

    /**
     * Update application status (user can only update their own).
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param applicationId Application ID
     * @param request Status update request with user notes
     * @return Updated application response
     */
    ApplicationResponse updateApplicationStatus(UUID userId, UUID applicationId, UpdateApplicationStatusRequest request);

    /**
     * Withdraw (soft delete) an application.
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param applicationId Application ID
     */
    void withdrawApplication(UUID userId, UUID applicationId);

    /**
     * Download application file (user can only access their own files).
     * Auth: Authenticated user
     *
     * @param userId User ID (from JWT token)
     * @param applicationId Application ID
     * @param fileType Type of file (resume, coverLetter, etc.)
     * @return File content as byte array
     */
    byte[] downloadApplicationFile(UUID userId, UUID applicationId, String fileType);

    // ==================== INTERNAL ENDPOINTS ====================

    /**
     * Get applications for a specific job post.
     * Auth: Internal service-to-service call
     *
     * @param jobPostId Job post ID
     * @param pageable Pagination parameters
     * @return Page of applications for the job post
     */
    Page<ApplicationResponse> getApplicationsByJobPost(UUID jobPostId, Pageable pageable);

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all applications in the system (with optional filters).
     * Auth: Admin only
     *
     * @param userId Optional filter by user ID
     * @param jobPostId Optional filter by job post ID
     * @param status Optional filter by status
     * @param pageable Pagination parameters
     * @return Page of applications
     */
    Page<ApplicationResponse> getAllApplications(UUID userId, UUID jobPostId, String status, Pageable pageable);

    /**
     * Get a specific application (admin access).
     * Auth: Admin only
     *
     * @param applicationId Application ID
     * @return Application response
     */
    ApplicationResponse getApplicationByIdAdmin(UUID applicationId);

    /**
     * Update application status with admin notes.
     * Auth: Admin only
     *
     * @param applicationId Application ID
     * @param request Status update request with admin notes
     * @return Updated application response
     */
    ApplicationResponse updateApplicationStatusAdmin(UUID applicationId, UpdateApplicationStatusAdminRequest request);

    /**
     * Hard delete an application (permanently remove from database).
     * Auth: Admin only
     *
     * @param applicationId Application ID
     */
    void deleteApplicationAdmin(UUID applicationId);

    /**
     * Get application statistics (total, by status, etc.).
     * Auth: Admin only
     *
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Application statistics
     */
    ApplicationStatisticsResponse getApplicationStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Bulk update application statuses.
     * Auth: Admin only
     *
     * @param request Bulk update request
     */
    void bulkUpdateApplicationStatus(BulkUpdateApplicationStatusRequest request);

    /**
     * Restore a soft-deleted application.
     * Auth: Admin only
     *
     * @param applicationId Application ID
     * @return Restored application response
     */
    ApplicationResponse restoreApplication(UUID applicationId);
}
