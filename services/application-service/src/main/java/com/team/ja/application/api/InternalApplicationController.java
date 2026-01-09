// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\api\InternalApplicationController.java

package com.team.ja.application.api;

import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.service.ApplicationService;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.enumeration.DocType;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for internal (service-to-service) application endpoints.
 * These endpoints are called by other microservices like job-manager service.
 * Not exposed in public API documentation.
 *
 * Auth: Internal service calls only (typically via Eureka or direct calls)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/job-posts")
@RequiredArgsConstructor
@Tag(name = "Applications - Internal", description = "Internal service-to-service communication endpoints")
public class InternalApplicationController {

    private final ApplicationService applicationService;

    /**
     * Get applications for a specific job post.
     * Called by job-manager service to fetch all applicants for a job post.
     *
     * @param jobPostId The job post ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param status Optional filter by application status
     * @return Page of applications for the job post
     */
    @GetMapping("/{jobPostId}/applications")
    @Operation(summary = "Get applications for job post", description = "Internal endpoint to fetch applications for a specific job post")
    public ApiResponse<Page<ApplicationResponse>> getApplicationsByJobPost(
            @Parameter(description = "Job post ID") @PathVariable UUID jobPostId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationResponse> response = applicationService.getApplicationsByJobPost(jobPostId, pageable);
        return ApiResponse.success("Applications retrieved successfully", response);
    }

    /**
     * Download application file (resume or cover letter).
     * Called by job-manager service to download applicant's documents.
     *
     * @param applicationId The application ID
     * @param docType Document type (RESUME or COVER_LETTER)
     * @return File as byte array
     */
    @GetMapping("/applications/{applicationId}/files/{docType}")
    @Operation(summary = "Download application file", description = "Internal endpoint to download resume or cover letter for an application")
    public ResponseEntity<?> downloadApplicationFile(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Document type: RESUME, COVER_LETTER") @PathVariable DocType docType) {
        
        log.info("Internal request to download {} for application: {}", docType, applicationId);
        
        try {
            byte[] fileContent = applicationService.downloadApplicationFileInternal(applicationId, docType.name());
            
            // Determine content type and filename based on doc type
            String contentType = "application/pdf";
            String fileName = docType.name().toLowerCase().replace("_", "-") + ".pdf";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            log.error("Error downloading {} for application {}: {}", docType, applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
