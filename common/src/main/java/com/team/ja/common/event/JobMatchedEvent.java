package com.team.ja.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a job posting matches a search profile.
 * 
 * Published from: user-service (when job matches search profile)
 * Topic: job-matched
 * 
 * Consumed by:
 * - notification-service: For sending notifications to matched applicants
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMatchedEvent {

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Schema(description = "The applicant/user who matched this job", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "The job post ID that matched", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID jobPostId;

    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String jobTitle;

    @Schema(description = "Job city", example = "Ho Chi Minh City")
    private String jobCity;

    @Schema(description = "Job country code", example = "VN")
    private String jobCountryCode;

    @Schema(description = "Timestamp when the match was detected", example = "2025-01-10T10:30:00")
    private LocalDateTime matchedAt;
}
