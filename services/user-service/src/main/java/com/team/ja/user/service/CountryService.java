package com.team.ja.user.service;

import com.team.ja.user.dto.response.CountryResponse;
import java.util.List;

/**
 * Service interface for Country operations.
 */
public interface CountryService {
    /**
     * Get all active countries.
     */
    List<CountryResponse> getAllCountries();

    /**
     * Get a country by its ID.
     */
    CountryResponse getCountryById(java.util.UUID id);

    /**
     * Get a country by its abbreviation (e.g., US, VN).
     */
    CountryResponse getCountryByAbbreviation(String abbreviation);

    /**
     * Search countries by name or abbreviation.
     */
    List<CountryResponse> searchCountries(String query);
}
