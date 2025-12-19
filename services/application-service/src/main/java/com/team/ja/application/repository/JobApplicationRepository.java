// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\repository\JobApplicationRepository.java

package com.team.ja.application.repository;

import com.team.ja.application.model.JobApplication;
import com.team.ja.common.enumeration.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for JobApplication entity.
 * Provides database operations for job applications.
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    /**
     * Find all applications submitted by a user (excluding soft-deleted).
     */
    Page<JobApplication> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    /**
     * Find all applications for a specific user with optional status filter.
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.userId = :userId AND ja.deletedAt IS NULL AND (:status IS NULL OR ja.status = :status)")
    Page<JobApplication> findUserApplicationsWithStatus(
            @Param("userId") UUID userId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    /**
     * Find all applications for a job post (excluding soft-deleted).
     */
    Page<JobApplication> findByJobPostIdAndDeletedAtIsNull(UUID jobPostId, Pageable pageable);

    /**
     * Find all applications for a job post with optional status filter.
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.jobPostId = :jobPostId AND ja.deletedAt IS NULL AND (:status IS NULL OR ja.status = :status)")
    Page<JobApplication> findJobPostApplicationsWithStatus(
            @Param("jobPostId") UUID jobPostId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    /**
     * Find all active applications (admin view).
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.deletedAt IS NULL")
    Page<JobApplication> findAllActiveApplications(Pageable pageable);

    /**
     * Find all active applications with multiple optional filters.
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.deletedAt IS NULL " +
           "AND (:userId IS NULL OR ja.userId = :userId) " +
           "AND (:jobPostId IS NULL OR ja.jobPostId = :jobPostId) " +
           "AND (:status IS NULL OR ja.status = :status)")
    Page<JobApplication> findApplicationsWithFilters(
            @Param("userId") UUID userId,
            @Param("jobPostId") UUID jobPostId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    /**
     * Count applications by status.
     */
    Long countByStatusAndDeletedAtIsNull(ApplicationStatus status);

    /**
     * Count total active applications.
     */
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.deletedAt IS NULL")
    Long countActiveApplications();

    /**
     * Find applications submitted within a date range.
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.deletedAt IS NULL AND ja.appliedAt BETWEEN :startDate AND :endDate")
    List<JobApplication> findApplicationsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find applications submitted this month.
     */
    @Query(value = "SELECT COUNT(*) FROM job_applications WHERE deleted_at IS NULL AND DATE_TRUNC('month', applied_at) = DATE_TRUNC('month', NOW())", nativeQuery = true)
    Long countApplicationsThisMonth();

    /**
     * Check if user has already applied for a job post.
     */
    @Query("SELECT COUNT(ja) > 0 FROM JobApplication ja WHERE ja.userId = :userId AND ja.jobPostId = :jobPostId AND ja.deletedAt IS NULL")
    boolean existsByUserIdAndJobPostId(@Param("userId") UUID userId, @Param("jobPostId") UUID jobPostId);

    /**
     * Find withdrawn applications (soft deleted by user).
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.deletedAt IS NOT NULL")
    Page<JobApplication> findDeletedApplications(Pageable pageable);
}
