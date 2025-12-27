package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import java.util.Optional;
import java.util.UUID;

public interface SearchProfileService {
    Optional<SearchProfileResponse> getByUserId(UUID userId);
}