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
}

