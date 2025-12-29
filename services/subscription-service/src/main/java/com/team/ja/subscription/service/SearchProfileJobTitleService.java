package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.request.CreateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.response.SearchProfileJobTitleResponse;

import java.util.List;
import java.util.UUID;

public interface SearchProfileJobTitleService {

    SearchProfileJobTitleResponse createJobTitle(UUID userId, CreateSearchProfileJobTitleRequest request);

    List<SearchProfileJobTitleResponse> getJobTitlesByUserId(UUID userId);

    SearchProfileJobTitleResponse getJobTitleById(UUID userId, UUID id);

    SearchProfileJobTitleResponse updateJobTitle(UUID userId, UUID id, UpdateSearchProfileJobTitleRequest request);

    void deleteJobTitle(UUID userId, UUID id);

}
