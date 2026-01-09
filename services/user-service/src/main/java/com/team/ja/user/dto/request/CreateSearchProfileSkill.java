package com.team.ja.user.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for user search profile skill")
public class CreateSearchProfileSkill {

    @NotEmpty(message = "At least one skill ID is required")
    @Schema(description = "List of skill IDs to add")
    private List<UUID> skillIds;

}
