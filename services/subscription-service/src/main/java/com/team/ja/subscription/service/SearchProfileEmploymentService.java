package com.team.ja.subscription.service;

import com.team.ja.subscription.dto.request.CreateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileEmploymentRequest;
import com.team.ja.subscription.dto.response.SearchProfileEmploymentResponse;

import java.util.List;
import java.util.UUID;

public interface SearchProfileEmploymentService {

    SearchProfileEmploymentResponse createEmployment(UUID userId, CreateSearchProfileEmploymentRequest request);

    List<SearchProfileEmploymentResponse> getEmploymentsByUserId(UUID userId);

    SearchProfileEmploymentResponse getEmploymentById(UUID userId, UUID id);

    SearchProfileEmploymentResponse updateEmployment(UUID userId, UUID id,
            UpdateSearchProfileEmploymentRequest request);

    void deleteEmployment(UUID userId, UUID id);

}
