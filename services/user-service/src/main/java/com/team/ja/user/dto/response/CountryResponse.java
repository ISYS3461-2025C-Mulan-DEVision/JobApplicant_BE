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
    
    @Schema(description = "Country Id", example = "69b65398-55d7-4d12-8378-e561b31c37c3")
    private String id;
}

