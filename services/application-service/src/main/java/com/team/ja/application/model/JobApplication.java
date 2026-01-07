// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\model\JobApplication.java

package com.team.ja.application.model;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JobApplication entity representing a job seeker's application to a job post.
 * 
 * Inherits from BaseEntity:
 * - id, createdAt, updatedAt
 * - isActive, deactivatedAt (for soft delete)
 * 
 * Relationships:
 * - userId: Reference to User entity (from user-service)
 * - jobPostId: Reference to JobPost entity (from job-manager subsystem)
 */
@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class JobApplication extends BaseEntity {

    /**
     * Reference to User entity (from user-service).
     * Nullable for testing purposes.
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Reference to JobPost entity (from job-manager subsystem).
     * Nullable for testing purposes.
     */
    @Column(name = "job_post_id")
    private UUID jobPostId;

    /**
     * Current status of the application.
     * See ApplicationStatus enum in common module for possible values.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /**
     * URL to the resume file stored in MinIO/S3.
     */
    @Column(columnDefinition = "TEXT")
    private String resumeUrl;

    /**
     * URL to the cover letter file stored in MinIO/S3.
     */
    @Column(columnDefinition = "TEXT")
    private String coverLetterUrl;

    /**
     * Timestamp when the application was submitted.
     */
    @Column(nullable = false)
    private LocalDateTime appliedAt;

    /**
     * Timestamp when the application status was last updated.
     */
    @Column
    private LocalDateTime applicationStatusUpdatedAt;

    /**
     * Notes from the applicant/user.
     * Can be provided when withdrawing or updating their application.
     */
    @Column(columnDefinition = "TEXT")
    private String userNotes;

    /**
     * Notes from admin users.
     * Typically added when updating application status.
     */
    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    /**
     * Notes from company users (HR/Recruiters).
     * Added when company manages the application in job-manager system.
     */
    @Column(columnDefinition = "TEXT")
    private String companyUserNotes;

    /**
     * Soft delete timestamp.
     * When set, the application is considered deleted but remains in the database.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Check if the application is deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Mark the application as deleted (soft delete).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Update the application status and mark the status update timestamp.
     */
    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
        this.applicationStatusUpdatedAt = LocalDateTime.now();
    }

    /**
     * Restore a deleted application.
     */
    public void restore() {
        this.deletedAt = null;
    }
}
