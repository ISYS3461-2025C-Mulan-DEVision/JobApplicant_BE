package com.team.ja.user.dto.request;

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
@Schema(description = "Request DTO for user search profile employment")
public class CreateSearchProfileEmployment {

    @Schema(description = "Search profile employment type")
    private EmploymentType employmentType;

}
