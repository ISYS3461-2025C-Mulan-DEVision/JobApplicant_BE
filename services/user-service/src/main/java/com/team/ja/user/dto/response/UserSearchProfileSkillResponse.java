package com.team.ja.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User search profile skill information")
public class UserSearchProfileSkillResponse {

    @Schema(description = "Skill ID", example = "1")
    private UUID skillId;

    @Schema(description = "Skill name", example = "Java")
    private String skillName;

    @Schema(description = "When the skill was added")
    private LocalDateTime createdAt;

}
