package com.team.ja.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skill response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Skill information")
public class SkillResponse {

    @Schema(description = "Skill name", example = "Java")
    private String name;

    @Schema(description = "Skill usage count (popularity)")
    private int usageCount;
}

