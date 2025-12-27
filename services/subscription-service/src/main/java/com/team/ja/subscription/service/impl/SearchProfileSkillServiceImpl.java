package com.team.ja.subscription.service.impl;

import com.team.ja.subscription.dto.request.CreateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;
import com.team.ja.subscription.mapper.SearchProfileSkillMapper;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.model.search_profile.SearchProfileSkill;
import com.team.ja.subscription.repository.SearchProfileRepository;
import com.team.ja.subscription.repository.SearchProfileSkillRepository;
import com.team.ja.subscription.service.SearchProfileSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import com.team.ja.common.exception.NotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProfileSkillServiceImpl implements SearchProfileSkillService {

    private final SearchProfileRepository searchProfileRepository;
    private final SearchProfileSkillRepository searchProfileSkillRepository;
    private final SearchProfileSkillMapper searchProfileSkillMapper;

    @Override
    @Transactional
    public SearchProfileSkillResponse createSkill(UUID userId, CreateSearchProfileSkillRequest request) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new SearchProfile();
            profile.setUserId(userId);
            profile = searchProfileRepository.save(profile);
        }

        // Check for existing skill record (including soft-deleted)
        java.util.Optional<SearchProfileSkill> existingOpt = searchProfileSkillRepository
                .findBySearchProfileIdAndSkillId(profile.getId(), request.getSkillId());

        if (existingOpt.isPresent()) {
            SearchProfileSkill existing = existingOpt.get();
            if (existing.isActive()) {
                // already present and active -> return existing
                return searchProfileSkillMapper.toResponse(existing);
            }
            // reactivate previously deleted record
            existing.activate();
            SearchProfileSkill reactivated = searchProfileSkillRepository.save(existing);
            return searchProfileSkillMapper.toResponse(reactivated);
        }

        SearchProfileSkill skill = new SearchProfileSkill();
        skill.setSkillId(request.getSkillId());
        skill.setUserId(userId);
        skill.setSearchProfile(profile);

        SearchProfileSkill saved = searchProfileSkillRepository.save(skill);
        return searchProfileSkillMapper.toResponse(saved);
    }

    @Override
    public List<SearchProfileSkillResponse> getSkillsByUserId(UUID userId) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            return List.of();
        }
        return searchProfileSkillRepository.findBySearchProfileIdAndIsActiveTrue(profile.getId())
                .stream()
                .map(searchProfileSkillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SearchProfileSkillResponse getSkillById(UUID userId, UUID id) {
        SearchProfileSkill skill = searchProfileSkillRepository.findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileSkill", "id", id.toString()));
        return searchProfileSkillMapper.toResponse(skill);
    }

    @Override
    @Transactional
    public SearchProfileSkillResponse updateSkill(UUID userId, UUID id, UpdateSearchProfileSkillRequest request) {
        SearchProfileSkill skill = searchProfileSkillRepository.findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileSkill", "id", id.toString()));
        skill.setSkillId(request.getSkillId());
        SearchProfileSkill updated = searchProfileSkillRepository.save(skill);
        return searchProfileSkillMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSkill(UUID userId, UUID id) {
        SearchProfileSkill skill = searchProfileSkillRepository.findByIdAndUserIdAndIsActiveTrue(id, userId)
                .orElseThrow(() -> new NotFoundException("SearchProfileSkill", "id", id.toString()));
        skill.deactivate();
        searchProfileSkillRepository.save(skill);
    }
}
