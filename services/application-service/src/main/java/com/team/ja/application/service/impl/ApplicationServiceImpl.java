// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\service\impl\ApplicationServiceImpl.java

package com.team.ja.application.service.impl;

import com.team.ja.application.config.S3FileService;
import com.team.ja.application.dto.request.BulkUpdateApplicationStatusRequest;
import com.team.ja.application.dto.request.CreateApplicationRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusAdminRequest;
import com.team.ja.application.dto.request.UpdateApplicationStatusRequest;
import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.dto.response.ApplicationStatisticsResponse;
import com.team.ja.application.mapper.ApplicationMapper;
import com.team.ja.application.model.JobApplication;
import com.team.ja.application.repository.JobApplicationRepository;
import com.team.ja.application.service.ApplicationService;
import com.team.ja.common.enumeration.ApplicationStatus;
import com.team.ja.common.enumeration.DocType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of ApplicationService.
 * Handles all business logic for job applications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final S3FileService s3FileService;

    // ==================== PUBLIC ENDPOINTS ====================

    @Override
    public ApplicationResponse createApplication(UUID userId, CreateApplicationRequest request) {
        log.info("Creating application for user: {} for job post: {}", userId, request.getJobPostId());

        // Check if user already applied for this job post
        if (applicationRepository.existsByUserIdAndJobPostId(userId, request.getJobPostId())) {
            throw new RuntimeException("You have already applied for this job post");
        }

        try {
            // Upload resume file
            String resumeUrl = s3FileService.uploadFile(request.getResumeFile(), "applications/resumes");

            // Upload cover letter file
            String coverLetterUrl = s3FileService.uploadFile(request.getCoverLetterFile(), "applications/cover-letters");

            // Create application entity
            JobApplication application = JobApplication.builder()
                    .userId(userId)
                    .jobPostId(request.getJobPostId())
                    .status(ApplicationStatus.SUBMITTED)
                    .resumeUrl(resumeUrl)
                    .coverLetterUrl(coverLetterUrl)
                    .appliedAt(LocalDateTime.now())
                    .applicationStatusUpdatedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            JobApplication savedApplication = applicationRepository.save(application);
            log.info("Application created successfully: {}", savedApplication.getId());

            return applicationMapper.toResponse(savedApplication);
        } catch (IOException e) {
            log.error("Error uploading files for application: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload application files", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getUserApplications(UUID userId, Pageable pageable) {
        log.info("Fetching applications for user: {}", userId);

        Page<JobApplication> applications = applicationRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        return applications.map(applicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID userId, UUID applicationId) {
        log.info("Fetching application: {} for user: {}", applicationId, userId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Verify ownership
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have access to this application");
        }

        return applicationMapper.toResponse(application);
    }

    @Override
    public ApplicationResponse updateApplicationStatus(UUID userId, UUID applicationId, UpdateApplicationStatusRequest request) {
        log.info("Updating application status: {} for user: {} to: {}", applicationId, userId, request.getStatus());

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Verify ownership
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have access to this application");
        }

        ApplicationStatus newStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        application.updateStatus(newStatus);
        if (request.getNotes() != null) {
            application.setUserNotes(request.getNotes());
        }

        JobApplication updatedApplication = applicationRepository.save(application);
        log.info("Application status updated successfully: {}", applicationId);

        return applicationMapper.toResponse(updatedApplication);
    }

    @Override
    public void withdrawApplication(UUID userId, UUID applicationId) {
        log.info("Withdrawing application: {} for user: {}", applicationId, userId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Verify ownership
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have access to this application");
        }

        application.softDelete();
        applicationRepository.save(application);
        log.info("Application withdrawn successfully: {}", applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadApplicationFile(UUID userId, UUID applicationId, String fileType) {
        log.info("Downloading file: {} for application: {} user: {}", fileType, applicationId, userId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Verify ownership
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have access to this application");
        }

        // Convert string to DocType enum
        DocType docType;
        try {
            docType = DocType.valueOf(fileType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid document type: " + fileType);
        }

        String fileUrl = null;
        switch (docType) {
            case RESUME:
                fileUrl = application.getResumeUrl();
                break;
            case COVER_LETTER:
                fileUrl = application.getCoverLetterUrl();
                break;
        }

        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new RuntimeException("File not found for type: " + fileType);
        }

        try {
            byte[] fileContent = s3FileService.downloadFile(fileUrl);
            log.info("File downloaded successfully: {} bytes", fileContent.length);
            return fileContent;
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    // ==================== INTERNAL ENDPOINTS ====================

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByJobPost(UUID jobPostId, Pageable pageable) {
        log.info("Fetching applications for job post: {}", jobPostId);

        Page<JobApplication> applications = applicationRepository.findByJobPostIdAndDeletedAtIsNull(jobPostId, pageable);
        return applications.map(applicationMapper::toResponse);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(UUID userId, UUID jobPostId, String status, Pageable pageable) {
        log.info("Fetching all applications with filters - userId: {}, jobPostId: {}, status: {}", userId, jobPostId, status);

        ApplicationStatus appStatus = null;
        if (status != null && !status.isEmpty()) {
            appStatus = ApplicationStatus.valueOf(status.toUpperCase());
        }

        Page<JobApplication> applications = applicationRepository.findApplicationsWithFilters(userId, jobPostId, appStatus, pageable);
        return applications.map(applicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationByIdAdmin(UUID applicationId) {
        log.info("Admin fetching application: {}", applicationId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        return applicationMapper.toResponse(application);
    }

    @Override
    public ApplicationResponse updateApplicationStatusAdmin(UUID applicationId, UpdateApplicationStatusAdminRequest request) {
        log.info("Admin updating application status: {} to: {}", applicationId, request.getStatus());

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStatus newStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        application.updateStatus(newStatus);
        if (request.getAdminNotes() != null) {
            application.setAdminNotes(request.getAdminNotes());
        }

        JobApplication updatedApplication = applicationRepository.save(application);
        log.info("Application status updated by admin successfully: {}", applicationId);

        return applicationMapper.toResponse(updatedApplication);
    }

    @Override
    public void deleteApplicationAdmin(UUID applicationId) {
        log.info("Admin hard deleting application: {}", applicationId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        applicationRepository.delete(application);
        log.info("Application deleted successfully: {}", applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationStatisticsResponse getApplicationStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching application statistics");

        // Set default date range if not provided
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();

        Long totalApplications = applicationRepository.countActiveApplications();
        Long applicationsThisMonth = applicationRepository.countApplicationsThisMonth();
        Long withdrawnApplications = applicationRepository.findDeletedApplications(Pageable.unpaged()).getTotalElements();

        // Get applications by status
        Map<String, Long> applicationsByStatus = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = applicationRepository.countByStatusAndDeletedAtIsNull(status);
            applicationsByStatus.put(status.toString(), count);
        }

        return ApplicationStatisticsResponse.builder()
                .totalApplications(totalApplications)
                .applicationsByStatus(applicationsByStatus)
                .applicationsThisMonth(applicationsThisMonth)
                .withdrawnApplications(withdrawnApplications)
                .build();
    }

    @Override
    public void bulkUpdateApplicationStatus(BulkUpdateApplicationStatusRequest request) {
        log.info("Bulk updating {} applications to status: {}", request.getApplicationIds().size(), request.getStatus());

        ApplicationStatus newStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());

        for (UUID applicationId : request.getApplicationIds()) {
            JobApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

            application.updateStatus(newStatus);
            if (request.getAdminNotes() != null) {
                application.setAdminNotes(request.getAdminNotes());
            }

            applicationRepository.save(application);
        }

        log.info("Bulk update completed successfully");
    }

    @Override
    public ApplicationResponse restoreApplication(UUID applicationId) {
        log.info("Restoring deleted application: {}", applicationId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.isDeleted()) {
            throw new RuntimeException("Application is not deleted");
        }

        application.restore();
        JobApplication restoredApplication = applicationRepository.save(application);
        log.info("Application restored successfully: {}", applicationId);

        return applicationMapper.toResponse(restoredApplication);
    }
    // ==================== HELPER METHODS ====================

}
