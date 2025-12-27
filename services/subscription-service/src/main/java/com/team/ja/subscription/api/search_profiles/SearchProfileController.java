package com.team.ja.subscription.api.search_profiles;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.service.SearchProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.team.ja.subscription.dto.request.UpdateSearchProfileRequest;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "SearchProfile", description = "Search profile endpoints")
public class SearchProfileController {

    private final SearchProfileService searchProfileService;

    /**
     * Get search profile by user ID
     * 
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/preferences")
    @Operation(summary = "Get search profile by user ID", description = "Retrieve the search profile associated with the specified user ID.")
    public ApiResponse<Optional<SearchProfileResponse>> getSearchProfileByUserId(@PathVariable("userId") UUID userId) {
        return ApiResponse.success(searchProfileService.getByUserId(userId));
    }

    @PutMapping("/{userId}/preferences")
    @Operation(summary = "Update search profile by user ID", description = "Update the search profile associated with the specified user ID.")
    public ApiResponse<Optional<SearchProfileResponse>> updateSearchProfile(@PathVariable("userId") UUID userId,
            @RequestBody UpdateSearchProfileRequest request) {
        return ApiResponse.success(searchProfileService.update(userId, request));
    }

}
