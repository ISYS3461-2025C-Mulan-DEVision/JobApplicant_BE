package com.team.ja.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create job title for search profile")
public class CreateSearchProfileJobTitleRequest {

    @NotBlank
    @Schema(description = "Job title text", example = "Software Engineer")
    private String title;

}
