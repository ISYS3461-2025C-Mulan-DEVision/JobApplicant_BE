package com.team.ja.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Country response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Country information")
public class CountryResponse {

    @Schema(description = "Country name", example = "Vietnam")
    private String name;

    @Schema(description = "Country abbreviation", example = "VN")
    private String abbreviation;
}

