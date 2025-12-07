package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.user.dto.response.CountryResponse;
import com.team.ja.user.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @Operation(summary = "Get all countries", description = "Retrieve all active countries")
    public ApiResponse<List<CountryResponse>> getAllCountries() {
        return ApiResponse.success(countryService.getAllCountries());
    }
}

