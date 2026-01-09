package com.team.ja.user.dto.response;

import java.util.UUID;

import com.team.ja.common.enumeration.EmploymentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User search profile employment information")
public class UserSearchProfileEmploymentResponse {

    @Schema(description = "Employment type ID")
    private UUID employmentTypeId;

    @Schema(description = "Search profile employment type")
    private EmploymentType employmentType;

}
