package com.team.ja.subscription.api.search_profiles;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.service.SearchProfileService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "SearchProfile", description = "Search profile endpoints")
public class SearchProfileController {

    private final SearchProfileService searchProfileService;

    @GetMapping("/{userId}/preferences")
    public ApiResponse<Optional<SearchProfileResponse>> getSearchProfileByUserId(@PathVariable("userId") UUID userId) {
        return ApiResponse.success(searchProfileService.getByUserId(userId));
    }

}
