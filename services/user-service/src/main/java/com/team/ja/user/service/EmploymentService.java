package com.team.ja.user.service;

import java.util.List;
import java.util.UUID;

import com.team.ja.user.dto.request.CreateSearchProfileEmployment;
import com.team.ja.user.dto.response.UserSearchProfileEmploymentResponse;

public interface EmploymentService {

    List<UserSearchProfileEmploymentResponse> addEmployment(CreateSearchProfileEmployment event, UUID userId);

    void removeEmploymentFromUserSearchProfile(UUID userId, UUID employmentId);

    List<UserSearchProfileEmploymentResponse> getEmploymentStatusByUserId(UUID userId);

}
