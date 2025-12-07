package com.team.ja.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User skill response DTO.
 * Represents a skill assigned to a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User skill information")
public class UserSkillResponse {

    @Schema(description = "Skill name", example = "Java")
    private String skillName;

    @Schema(description = "When the skill was added")
    private LocalDateTime createdAt;
}

