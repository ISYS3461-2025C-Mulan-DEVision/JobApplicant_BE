package com.team.ja.subscription.api.search_profiles;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.subscription.dto.request.CreateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.request.UpdateSearchProfileSkillRequest;
import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;
import com.team.ja.subscription.service.SearchProfileSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription/{userId}/preferences/skills")
@RequiredArgsConstructor
@Tag(name = "SearchProfile Skills", description = "Manage search profile skills")
public class SearchProfileSkillController {

    private final SearchProfileSkillService searchProfileSkillService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add skill to search profile")
    public ApiResponse<SearchProfileSkillResponse> createSkill(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody CreateSearchProfileSkillRequest request) {
        return ApiResponse.success("Skill added", searchProfileSkillService.createSkill(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all skills for user")
    public ApiResponse<List<SearchProfileSkillResponse>> getSkills(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        return ApiResponse.success(searchProfileSkillService.getSkillsByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by id")
    public ApiResponse<SearchProfileSkillResponse> getSkillById(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Skill record ID") @PathVariable UUID id) {
        return ApiResponse.success(searchProfileSkillService.getSkillById(userId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill")
    public ApiResponse<SearchProfileSkillResponse> updateSkill(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Skill record ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSearchProfileSkillRequest request) {
        return ApiResponse.success("Skill updated", searchProfileSkillService.updateSkill(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete skill from profile")
    public ApiResponse<Void> deleteSkill(@Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Skill record ID") @PathVariable UUID id) {
        searchProfileSkillService.deleteSkill(userId, id);
        return ApiResponse.success("Skill deleted", null);
    }
}
