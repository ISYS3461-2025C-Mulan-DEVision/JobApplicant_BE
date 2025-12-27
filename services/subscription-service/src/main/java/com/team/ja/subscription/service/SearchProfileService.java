package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.dto.request.UpdateSearchProfileRequest;
import java.util.Optional;
import java.util.UUID;

public interface SearchProfileService {
    Optional<SearchProfileResponse> getByUserId(UUID userId);

    Optional<SearchProfileResponse> update(UUID userId, UpdateSearchProfileRequest request);
}