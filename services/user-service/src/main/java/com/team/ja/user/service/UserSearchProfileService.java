package com.team.ja.user.service;

import java.util.UUID;

import com.team.ja.user.dto.request.CreateSearchProfile;
import com.team.ja.user.dto.request.UpdateSearchProfile;
import com.team.ja.user.dto.response.UserSearchProfileResponse;

public interface UserSearchProfileService {

    UserSearchProfileResponse createUserSearchProfile(CreateSearchProfile request, UUID userId);

    UserSearchProfileResponse getUserSearchProfileByUserId(UUID userId);

    UserSearchProfileResponse updateUserSearchProfile(UUID userId, UpdateSearchProfile request);

    void deactivateUserSearchProfile(UUID userId);

}
