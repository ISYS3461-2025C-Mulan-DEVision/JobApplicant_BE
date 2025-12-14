package com.team.ja.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated user profile response containing all user data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Complete user profile with all related data")
public class UserProfileResponse {

    @Schema(description = "Basic user information")
    private UserResponse user;

    @Schema(description = "User's education history")
    private List<UserEducationResponse> education;

    @Schema(description = "User's work experience history")
    private List<UserWorkExperienceResponse> workExperience;

    @Schema(description = "User's skills")
    private List<SkillResponse> skills;
}

