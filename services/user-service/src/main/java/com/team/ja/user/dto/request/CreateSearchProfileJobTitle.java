package com.team.ja.user.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for user search profile job titles")
public class CreateSearchProfileJobTitle {

    @Schema(description = "Job titles to add")
    private List<String> jobTitles;

}
