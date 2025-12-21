package com.team.ja.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Skill information")
public class SkillResponse {

    @Schema(description = "Skill ID")
    private String id;

    @Schema(description = "Skill name", example = "Java")
    private String name;

    @Schema(description = "Skill usage count (popularity)")
    private int usageCount;

}
