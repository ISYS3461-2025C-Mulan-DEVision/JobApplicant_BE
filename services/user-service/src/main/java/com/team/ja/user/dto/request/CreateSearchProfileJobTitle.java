package com.team.ja.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for user search profile job title")
public class CreateSearchProfileJobTitle {

    @Schema(description = "Job title to add")
    private String jobTitle;

}
