package com.team.ja.user.dto.request;

import java.math.BigDecimal;

import com.team.ja.common.enumeration.EducationLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create user search profile request")
public class CreateSearchProfile {

    @Schema(description = "Minimum desired salary for the search profile")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum desired salary for the search profile")
    private BigDecimal salaryMax;

    @Schema(description = "Country abbreviation associated with this search profile")
    private String countryAbbreviation;

    @Schema(description = "Highest education level associated with this search profile")
    private EducationLevel educationLevel;

}
