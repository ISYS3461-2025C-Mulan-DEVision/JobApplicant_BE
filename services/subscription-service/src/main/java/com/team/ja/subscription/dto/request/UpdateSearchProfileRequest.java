package com.team.ja.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for updating a search profile.
 * All fields are optional — only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update search profile request")
public class UpdateSearchProfileRequest {

    @Schema(description = "Country ID to search in")
    private UUID countryId;

    @Schema(description = "Minimum desired salary")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum desired salary")
    private BigDecimal salaryMax;

    @Schema(description = "Desired job title")
    private String jobTitle;

}
