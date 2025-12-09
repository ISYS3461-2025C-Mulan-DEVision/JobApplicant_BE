package com.team.ja.user.dto.request;

import com.team.ja.common.enumeration.EmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating user work experience entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create user work experience request")
public class CreateUserWorkExperienceRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title must not exceed 255 characters")
    @Schema(description = "Job title", example = "Software Engineer")
    private String jobTitle;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Schema(description = "Company name", example = "Google")
    private String companyName;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private EmploymentType employmentType;

    @Schema(description = "Country ID where the job is located")
    private UUID countryId;

    @Schema(description = "Start date", example = "2022-01-15")
    private LocalDate startAt;

    @Schema(description = "End date (null if current job)", example = "2024-06-30")
    private LocalDate endAt;

    @Schema(description = "Whether this is current job", example = "false")
    private boolean current;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Job description")
    private String description;
}
