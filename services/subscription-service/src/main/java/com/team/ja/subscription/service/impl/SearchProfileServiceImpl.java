package com.team.ja.subscription.service.impl;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import com.team.ja.subscription.repository.SearchProfileRepository;
import com.team.ja.subscription.service.SearchProfileService;
import com.team.ja.subscription.mapper.SearchProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

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
}
