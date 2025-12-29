package com.team.ja.subscription.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.subscription.dto.request.CreateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.response.SearchProfileJobTitleResponse;
import com.team.ja.subscription.mapper.SearchProfileJobTitleMapper;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.model.search_profile.SearchProfileJobTitle;
import com.team.ja.subscription.repository.SearchProfileJobTitleRepository;
import com.team.ja.subscription.repository.SearchProfileRepository;
import com.team.ja.subscription.service.SearchProfileJobTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProfileJobTitleServiceImpl implements SearchProfileJobTitleService {

    private final SearchProfileRepository searchProfileRepository;
    private final SearchProfileJobTitleRepository searchProfileJobTitleRepository;
    private final SearchProfileJobTitleMapper searchProfileJobTitleMapper;

    @Override
    @Transactional
    public SearchProfileJobTitleResponse createJobTitle(UUID userId, CreateSearchProfileJobTitleRequest request) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = new SearchProfile();
            profile.setUserId(userId);
            profile = searchProfileRepository.save(profile);
        }

        // Create and save
        SearchProfileJobTitle jt = new SearchProfileJobTitle();
        jt.setTitle(request.getTitle());
        jt.setUserId(userId);
        jt.setSearchProfile(profile);

        SearchProfileJobTitle saved = searchProfileJobTitleRepository.save(jt);
        return searchProfileJobTitleMapper.toResponse(saved);
    }

    @Override
    public List<SearchProfileJobTitleResponse> getJobTitlesByUserId(UUID userId) {
        SearchProfile profile = searchProfileRepository.findByUserId(userId);
        if (profile == null) {
            return List.of();
        }
        return searchProfileJobTitleRepository.findBySearchProfileId(profile.getId())
                .stream()
                .filter(SearchProfileJobTitle::isActive)
                .map(searchProfileJobTitleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SearchProfileJobTitleResponse getJobTitleById(UUID userId, UUID id) {
        SearchProfileJobTitle jt = searchProfileJobTitleRepository.findById(id)
                .filter(SearchProfileJobTitle::isActive)
                .orElseThrow(() -> new NotFoundException("SearchProfileJobTitle", "id", id.toString()));
        if (!jt.getUserId().equals(userId)) {
            throw new NotFoundException("SearchProfileJobTitle", "id", id.toString());
        }
        return searchProfileJobTitleMapper.toResponse(jt);
    }

    @Override
    @Transactional
    public SearchProfileJobTitleResponse updateJobTitle(UUID userId, UUID id,
            UpdateSearchProfileJobTitleRequest request) {
        SearchProfileJobTitle jt = searchProfileJobTitleRepository.findById(id)
                .filter(SearchProfileJobTitle::isActive)
                .orElseThrow(() -> new NotFoundException("SearchProfileJobTitle", "id", id.toString()));
        if (!jt.getUserId().equals(userId)) {
            throw new NotFoundException("SearchProfileJobTitle", "id", id.toString());
        }
        if (request.getTitle() != null) {
            jt.setTitle(request.getTitle());
        }
        if (request.getIsActive() != null) {
            if (request.getIsActive())
                jt.activate();
            else
                jt.deactivate();
        }
        SearchProfileJobTitle updated = searchProfileJobTitleRepository.save(jt);
        return searchProfileJobTitleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteJobTitle(UUID userId, UUID titleId) {
        SearchProfileJobTitle jobTitle = searchProfileJobTitleRepository.findById(titleId)
                .filter(SearchProfileJobTitle::isActive)
                .orElseThrow(() -> new NotFoundException("SearchProfileJobTitle", "id", titleId.toString()));
        if (!jobTitle.getUserId().equals(userId)) {
            throw new NotFoundException("SearchProfileJobTitle", "id", titleId.toString());
        }
        jobTitle.deactivate();
        searchProfileJobTitleRepository.save(jobTitle);
    }
}
