package com.team.ja.user.service.impl;

import com.team.ja.user.dto.response.CountryResponse;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.team.ja.user.model.Country;

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
        List<Country> countries = countryRepository.findByIsActiveTrueOrderByNameAsc();
        log.debug("Found {} active countries from repository.", countries.size());
        return countryMapper.toResponseList(countries);
    }
}
