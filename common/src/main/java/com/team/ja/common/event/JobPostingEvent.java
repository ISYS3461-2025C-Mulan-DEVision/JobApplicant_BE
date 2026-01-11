package com.team.ja.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a new job post is created or published.
 * 
 * Published from: Job Manager service
 * Topics: jobpost.published, jobpost.skills.changed, jobpost.country.changed
 * 
 * Consumed by:
 * - user-service: For real-time job matching against search profiles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingEvent {

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Schema(description = "Job post ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID jobPostId;

    @Schema(description = "Job Manager ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID jobManagerId;

    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String title;

    @Schema(description = "Job description", example = "We are looking for a senior engineer...")
    private String description;

    @Schema(description = "List of required skill IDs", example = "[\"550e8400-e29b-41d4-a716-446655440002\"]")
    private List<UUID> requiredSkillIds;

    @Schema(description = "List of employment types", example = "[\"FULL_TIME\", \"CONTRACT\"]")
    private List<String> employmentTypes;

    @Schema(description = "Minimum salary for the job", example = "50000")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary for the job", example = "100000")
    private BigDecimal salaryMax;

    @Schema(description = "Country code (2-letter)", example = "VN")
    private String countryCode;

    @Schema(description = "City name", example = "Ho Chi Minh City")
    private String city;

    @Schema(description = "Is this job open for freshers", example = "true")
    private Boolean fresher;

    @Schema(description = "Timestamp when the job was published", example = "2025-01-10T10:30:00")
    private LocalDateTime publishedAt;

    @Schema(description = "Is the job currently published and active", example = "true")
    private Boolean published;
}
