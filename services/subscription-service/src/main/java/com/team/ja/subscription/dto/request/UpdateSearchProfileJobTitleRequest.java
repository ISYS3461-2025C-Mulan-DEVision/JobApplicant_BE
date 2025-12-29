package com.team.ja.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update job title for search profile")
public class UpdateSearchProfileJobTitleRequest {

    @Schema(description = "Job title text", example = "Backend Developer")
    private String title;

    @Schema(description = "Active flag")
    private Boolean isActive;

}
