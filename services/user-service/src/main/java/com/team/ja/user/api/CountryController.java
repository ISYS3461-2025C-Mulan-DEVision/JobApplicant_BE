package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.user.dto.response.CountryResponse;
import com.team.ja.user.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Country reference data.
 */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Country", description = "Country reference data endpoints")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    @Operation(
        summary = "Get all countries",
        description = "Retrieve all active countries"
    )
    public ApiResponse<List<CountryResponse>> getAllCountries() {
        return ApiResponse.success(countryService.getAllCountries());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get country by ID",
        description = "Retrieve a country by its UUID"
    )
    public ApiResponse<CountryResponse> getCountryById(
        @Parameter(description = "Country ID") @PathVariable java.util.UUID id
    ) {
        return ApiResponse.success(countryService.getCountryById(id));
    }

    @GetMapping("/abbr/{abbreviation}")
    @Operation(
        summary = "Get country by abbreviation",
        description = "Retrieve a country by its abbreviation, e.g., US, VN"
    )
    public ApiResponse<CountryResponse> getCountryByAbbreviation(
        @Parameter(
            description = "Country abbreviation"
        ) @PathVariable String abbreviation
    ) {
        return ApiResponse.success(
            countryService.getCountryByAbbreviation(abbreviation)
        );
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search countries",
        description = "Search countries by name or abbreviation"
    )
    public ApiResponse<List<CountryResponse>> searchCountries(
        @Parameter(
            description = "Search query for name or abbreviation"
        ) @RequestParam(required = false) String q
    ) {
        return ApiResponse.success(countryService.searchCountries(q));
    }
}
