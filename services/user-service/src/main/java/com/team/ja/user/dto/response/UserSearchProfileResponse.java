package com.team.ja.user.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.team.ja.common.enumeration.EducationLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User search profile information")
public class UserSearchProfileResponse {

    @Schema(description = "Unique identifier of the search profile")
    private UUID searchProfileId;

    @Schema(description = "Unique identifier of the user associated with this search profile")
    private UUID userId;

    @Schema(description = "Minimum desired salary for the search profile")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum desired salary for the search profile")
    private BigDecimal salaryMax;

    @Schema(description = "Country abbreviation associated with this search profile")
    private String countryAbbreviation;

    @Schema(description = "Is Fresher flag for this search profile")
    private Boolean isFresher;

    @Schema(description = "Highest education level associated with this search profile")
    private EducationLevel educationLevel;

    @Schema(description = "List of skills associated with this search profile")
    private List<UserSearchProfileSkillResponse> skills;

    @Schema(description = "List of employment types associated with this search profile")
    private List<UserSearchProfileEmploymentResponse> employments;

    @Schema(description = "List of job titles associated with this search profile")
    private List<UserSearchProfileJobTitleResponse> jobTitles;

}
