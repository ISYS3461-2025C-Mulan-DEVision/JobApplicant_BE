package com.team.ja.user.dto.response;

import com.team.ja.common.enumeration.EmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User work experience response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User work experience information")
public class UserWorkExperienceResponse {

    @Schema(description = "Job title", example = "Software Engineer")
    private String jobTitle;

    @Schema(description = "Company name", example = "Google")
    private String companyName;

    @Schema(description = "Employment type")
    private EmploymentType employmentType;

    @Schema(description = "Employment type display name", example = "Full-time")
    private String employmentTypeDisplayName;

    @Schema(description = "Start date")
    private LocalDate startAt;

    @Schema(description = "End date (null if current job)")
    private LocalDate endAt;

    @Schema(description = "Whether this is current job")
    private boolean isCurrent;

    @Schema(description = "Job description")
    private String description;

    // Nested object
    @Schema(description = "Country where the job is located")
    private CountryResponse country;
}

