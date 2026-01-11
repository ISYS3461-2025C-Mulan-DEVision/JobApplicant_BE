package com.team.ja.user.service;

import java.util.List;
import java.util.UUID;

import com.team.ja.user.dto.request.CreateSearchProfileJobTitle;
import com.team.ja.user.dto.response.UserSearchProfileJobTitleResponse;

public interface UserSearchProfileJobTitleService {

    List<UserSearchProfileJobTitleResponse> createUserSearchProfileJobTitle(CreateSearchProfileJobTitle request,
            UUID searchProfileId, UUID userId);

    void deleteUserSearchProfileJobTitle(UUID searchProfileId, UUID jobTitleId);

    List<UserSearchProfileJobTitleResponse> getUserSearchProfileJobTitles(UUID searchProfileId);

}
