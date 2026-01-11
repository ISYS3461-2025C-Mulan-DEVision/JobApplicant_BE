package com.team.ja.user.dto.response;

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
@Schema(description = "User search profile job title information")
public class UserSearchProfileJobTitleResponse {

    @Schema(description = "Job title ID")
    private UUID jobTitleId;

    @Schema(description = "Job title associated with this search profile")
    private String jobTitle;

}
