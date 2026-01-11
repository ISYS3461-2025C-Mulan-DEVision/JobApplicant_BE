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
@Schema(description = "Update user search profile request")
public class UpdateSearchProfile {

    @Schema(description = "Minimum desired salary for the search profile", example = "50000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum desired salary for the search profile", example = "100000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Country abbreviation associated with this search profile", example = "US")
    private String countryAbbreviation;

    @Schema(description = "Highest education level associated with this search profile", example = "MASTER")
    private EducationLevel educationLevel;

    @Schema(description = "Is Fresher flag for this search profile", example = "false")
    private Boolean isFresher;

}
