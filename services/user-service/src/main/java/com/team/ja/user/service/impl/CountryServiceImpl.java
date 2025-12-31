package com.team.ja.user.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.dto.response.CountryResponse;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.model.Country;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.service.CountryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CountryService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Override
    public List<CountryResponse> getAllCountries() {
        log.info("Fetching all active countries");
        List<Country> countries =
            countryRepository.findByIsActiveTrueOrderByNameAsc();
        log.debug(
            "Found {} active countries from repository.",
            countries.size()
        );
        return countryMapper.toResponseList(countries);
    }

    @Override
    public CountryResponse getCountryById(UUID id) {
        log.info("Fetching country by id={}", id);
        Country country = countryRepository
            .findById(id)
            .orElseThrow(() ->
                new NotFoundException("Country", "id", id.toString())
            );
        return countryMapper.toResponse(country);
    }

    @Override
    public CountryResponse getCountryByAbbreviation(String abbreviation) {
        log.info("Fetching country by abbreviation={}", abbreviation);
        Country country = countryRepository
            .findByAbbreviationIgnoreCase(abbreviation)
            .orElseThrow(() ->
                new NotFoundException("Country", "abbreviation", abbreviation)
            );
        return countryMapper.toResponse(country);
    }

    @Override
    public List<CountryResponse> searchCountries(String query) {
        log.info("Searching countries by query='{}'", query);
        // simple in-memory filtering across active countries by name or abbreviation
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Country> countries =
            countryRepository.findByIsActiveTrueOrderByNameAsc();
        List<Country> filtered = countries
            .stream()
            .filter(
                c ->
                    q.isEmpty() ||
                    (c.getName() != null &&
                        c.getName().toLowerCase().contains(q)) ||
                    (c.getAbbreviation() != null &&
                        c.getAbbreviation().toLowerCase().contains(q))
            )
            .toList();
        log.debug("Search '{}' matched {} countries.", q, filtered.size());
        return countryMapper.toResponseList(filtered);
    }
}
