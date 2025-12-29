package com.team.ja.subscription.api.search_profiles;

import com.team.ja.subscription.dto.request.CreateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileJobTitleRequest;
import com.team.ja.subscription.dto.response.SearchProfileJobTitleResponse;
import com.team.ja.subscription.service.SearchProfileJobTitleService;
import com.team.ja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription/{userId}/preferences/job-titles")
@Tag(name = "SearchProfile Job Titles", description = "Manage search profile job titles")
@RequiredArgsConstructor
public class SearchProfileJobTitleController {

    private final SearchProfileJobTitleService searchProfileJobTitleService;

    @PostMapping
    public ApiResponse<SearchProfileJobTitleResponse> createJobTitle(@PathVariable UUID userId,
            @Valid @RequestBody CreateSearchProfileJobTitleRequest request) {
        return ApiResponse.success("Job title added",
                searchProfileJobTitleService.createJobTitle(userId, request));
    }

    @GetMapping
    public ApiResponse<List<SearchProfileJobTitleResponse>> getJobTitles(@PathVariable UUID userId) {
        return ApiResponse.success(searchProfileJobTitleService.getJobTitlesByUserId(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SearchProfileJobTitleResponse> getJobTitleById(@PathVariable UUID userId,
            @PathVariable UUID id) {
        return ApiResponse.success(searchProfileJobTitleService.getJobTitleById(userId, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SearchProfileJobTitleResponse> updateJobTitle(@PathVariable UUID userId,
            @PathVariable UUID id, @Valid @RequestBody UpdateSearchProfileJobTitleRequest request) {
        return ApiResponse.success("Job title updated",
                searchProfileJobTitleService.updateJobTitle(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteJobTitle(@PathVariable UUID userId, @PathVariable UUID id) {
        searchProfileJobTitleService.deleteJobTitle(userId, id);
        return ApiResponse.success("Job title deleted", null);
    }
}
