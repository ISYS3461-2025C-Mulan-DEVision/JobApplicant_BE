package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.request.CreateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;

import java.util.List;
import java.util.UUID;

public interface SearchProfileSkillService {

    SearchProfileSkillResponse createSkill(UUID userId, CreateSearchProfileSkillRequest request);

    List<SearchProfileSkillResponse> getSkillsByUserId(UUID userId);

    SearchProfileSkillResponse getSkillById(UUID userId, UUID id);

    SearchProfileSkillResponse updateSkill(UUID userId, UUID id, UpdateSearchProfileSkillRequest request);

    void deleteSkill(UUID userId, UUID id);

}
