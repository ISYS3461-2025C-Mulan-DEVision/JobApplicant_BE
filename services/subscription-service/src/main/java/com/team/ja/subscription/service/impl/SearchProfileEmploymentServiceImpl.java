package com.team.ja.subscription.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.subscription.dto.request.CreateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.response.SearchProfileEmploymentResponse;
import com.team.ja.subscription.model.search_profile.SearchProfileEmployment;
import com.team.ja.subscription.mapper.SearchProfileEmploymentMapper;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.model.search_profile.SearchProfileEmployment;
import com.team.ja.subscription.repository.SearchProfileEmploymentRepository;
import com.team.ja.subscription.repository.SearchProfileRepository;
import com.team.ja.subscription.service.SearchProfileEmploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProfileEmploymentServiceImpl implements SearchProfileEmploymentService {

    private final SearchProfileRepository searchProfileRepository;
    private final SearchProfileEmploymentRepository searchProfileEmploymentRepository;
    private final SearchProfileEmploymentMapper searchProfileEmploymentMapper;

    @Override
    @Transactional
    public SearchProfileEmploymentResponse createEmployment(UUID userId, CreateSearchProfileEmploymentRequest request) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new SearchProfile();
            profile.setUserId(userId);
            profile = searchProfileRepository.save(profile);
        }

        // Check existing (including soft-deleted)
        java.util.Optional<SearchProfileEmployment> existingOpt = searchProfileEmploymentRepository
                .findBySearchProfileIdAndEmploymentType(profile.getId(), request.getEmploymentType());

        if (existingOpt.isPresent()) {
            SearchProfileEmployment existing = existingOpt.get();
            if (existing.isActive()) {
                return searchProfileEmploymentMapper.toResponse(existing);
            }
            existing.activate();
            SearchProfileEmployment reactivated = searchProfileEmploymentRepository.save(existing);
            return searchProfileEmploymentMapper.toResponse(reactivated);
        }

        SearchProfileEmployment employment = new SearchProfileEmployment();
        employment.setEmploymentType(request.getEmploymentType());
        employment.setUserId(userId);
        employment.setSearchProfile(profile);

        SearchProfileEmployment saved = searchProfileEmploymentRepository.save(employment);
        return searchProfileEmploymentMapper.toResponse(saved);
    }

    @Override
    public List<SearchProfileEmploymentResponse> getEmploymentsByUserId(UUID userId) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            return List.of();
        }
        return searchProfileEmploymentRepository.findBySearchProfileIdAndIsActiveTrue(profile.getId())
                .stream()
                .map(searchProfileEmploymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SearchProfileEmploymentResponse getEmploymentById(UUID userId, UUID id) {
        SearchProfileEmployment emp = searchProfileEmploymentRepository
                .findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileEmployment", "id", id.toString()));
        return searchProfileEmploymentMapper.toResponse(emp);
    }

    @Override
    @Transactional
    public SearchProfileEmploymentResponse updateEmployment(UUID userId, UUID id,
            UpdateSearchProfileEmploymentRequest request) {
        SearchProfileEmployment emp = searchProfileEmploymentRepository
                .findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileEmployment", "id", id.toString()));
        emp.setEmploymentType(request.getEmploymentType());
        SearchProfileEmployment updated = searchProfileEmploymentRepository.save(emp);
        return searchProfileEmploymentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEmployment(UUID userId, UUID id) {
        SearchProfileEmployment emp = searchProfileEmploymentRepository
                .findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileEmployment", "id", id.toString()));
        emp.deactivate();
        searchProfileEmploymentRepository.save(emp);
    }
}
