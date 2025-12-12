package com.team.ja.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for adding skills to a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Add user skills request")
public class AddUserSkillsRequest {

    @NotEmpty(message = "At least one skill ID is required")
    @Schema(description = "List of skill IDs to add")
    private List<UUID> skillIds;
}
