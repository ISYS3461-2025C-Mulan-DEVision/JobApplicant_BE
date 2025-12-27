package com.team.ja.subscription.service.impl;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.dto.request.UpdateSearchProfileRequest;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.repository.SearchProfileRepository;
import com.team.ja.subscription.service.SearchProfileService;
import com.team.ja.subscription.mapper.SearchProfileMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;



@Service
@RequiredArgsConstructor
public class SearchProfileServiceImpl implements SearchProfileService {

    private final SearchProfileRepository searchProfileRepository;
    private final SearchProfileMapper searchProfileMapper;

    @Override
    public Optional<SearchProfileResponse> getByUserId(UUID userId) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            return Optional.empty();
        }
        return Optional.of(searchProfileMapper.toResponse(profile));
    }

    @Override
    @Transactional
    public Optional<SearchProfileResponse> update(UUID userId, UpdateSearchProfileRequest request) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            return Optional.empty();
        }

        if (request.getCountryId() != null) {
            profile.setCountryId(request.getCountryId());
        }
        if (request.getSalaryMin() != null) {
            profile.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            profile.setSalaryMax(request.getSalaryMax());
        }
        if (request.getJobTitle() != null) {
            profile.setJobTitle(request.getJobTitle());
        }

        searchProfileRepository.save(profile);

        return Optional.of(searchProfileMapper.toResponse(profile));
    }
}
