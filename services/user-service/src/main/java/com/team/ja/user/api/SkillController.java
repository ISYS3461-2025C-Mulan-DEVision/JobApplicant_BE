package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.user.dto.request.AddUserSkillsRequest;
import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Skill operations.
 * 
 * Skills are reference data that users can select from.
 */
@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skill reference data and user skill management")
public class SkillController {

    private final SkillService skillService;

    // ==================== SKILL REFERENCE DATA ====================

    @GetMapping
    @Operation(summary = "Get all skills", description = "Get all active skills sorted by popularity")
    public ApiResponse<List<SkillResponse>> getAllSkills() {
        return ApiResponse.success(skillService.getAllSkills());
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular skills", description = "Get top 20 most used skills")
    public ApiResponse<List<SkillResponse>> getPopularSkills() {
        return ApiResponse.success(skillService.getPopularSkills());
    }

    @GetMapping("/search")
    @Operation(summary = "Search skills", description = "Search skills by name")
    public ApiResponse<List<SkillResponse>> searchSkills(
            @Parameter(description = "Search query") @RequestParam(required = false) String q) {
        return ApiResponse.success(skillService.searchSkills(q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID", description = "Get a skill by its ID")
    public ApiResponse<SkillResponse> getSkillById(
            @Parameter(description = "Skill ID") @PathVariable UUID id) {
        return ApiResponse.success(skillService.getSkillById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create skill", description = "Create a new skill (Admin only in production)")
    public ApiResponse<SkillResponse> createSkill(
            @Parameter(description = "Skill name") @RequestParam String name) {
        return ApiResponse.success("Skill created successfully", skillService.createSkill(name));
    }

    // ==================== USER SKILLS ====================

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user skills", description = "Get all skills for a user")
    public ApiResponse<List<SkillResponse>> getUserSkills(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        return ApiResponse.success(skillService.getUserSkills(userId));
    }

    @PostMapping("/users/{userId}")
    @Operation(summary = "Add skills to user", description = "Add skills to a user's profile")
    public ApiResponse<List<SkillResponse>> addSkillsToUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody AddUserSkillsRequest request) {
        return ApiResponse.success(
                "Skills added successfully",
                skillService.addSkillsToUser(userId, request.getSkillIds()));
    }

    @DeleteMapping("/users/{userId}/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove skill from user", description = "Remove a skill from a user's profile")
    public ApiResponse<Void> removeSkillFromUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Skill ID") @PathVariable UUID skillId) {
        skillService.removeSkillFromUser(userId, skillId);
        return ApiResponse.success("Skill removed successfully", null);
    }
}

