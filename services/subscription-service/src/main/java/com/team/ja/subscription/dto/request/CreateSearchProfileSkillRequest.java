package com.team.ja.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create search profile skill request")
public class CreateSearchProfileSkillRequest {

    @NotNull
    @Schema(description = "Skill id to add", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID skillId;

}
