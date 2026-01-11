package com.team.ja.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.team.ja.common.enumeration.EmploymentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user updates their search profile.
 * 
 * This will link directly to JM applicant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchProfileUpdateEvent {

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Schema(description = "User ID associated with the profile")
    private UUID userId;

    @Schema(description = "Country abbreviation (2-letter) for the user")
    private String countryAbbreviation;

    @Schema(description = "Highest education level of the user", example = "BACHELORS")
    private String educationLevel;

    @Schema(description = "List of skill IDs associated with the user")
    private List<UUID> skillIds;

    @Schema(description = "List of employment types preferred by the user")
    private List<String> employmentTypes;

    @Schema(description = "Minimum salary preference of the user", example = "50000")
    private BigDecimal minSalary;

    @Schema(description = "Maximum salary preference of the user", example = "100000")
    private BigDecimal maxSalary;

    @Schema(description = "Is Fresher flag for the user")
    private Boolean isFresher;

    // Not required but also useful
    @Schema(description = "List of job titles associated with the user")
    private List<String> jobTitles;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
