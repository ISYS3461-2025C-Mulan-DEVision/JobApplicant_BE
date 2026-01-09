package com.team.ja.user.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.user.dto.request.CreateSearchProfile;
import com.team.ja.user.dto.request.CreateSearchProfileEmployment;
import com.team.ja.user.dto.request.CreateSearchProfileJobTitle;
import com.team.ja.user.dto.request.CreateSearchProfileSkill;
import com.team.ja.user.dto.request.UpdateSearchProfile;
import com.team.ja.user.dto.response.UserSearchProfileEmploymentResponse;
import com.team.ja.user.dto.response.UserSearchProfileJobTitleResponse;
import com.team.ja.user.dto.response.UserSearchProfileResponse;
import com.team.ja.user.dto.response.UserSearchProfileSkillResponse;
import com.team.ja.user.service.EmploymentService;
import com.team.ja.user.service.SkillService;
import com.team.ja.user.service.UserSearchProfileJobTitleService;
import com.team.ja.user.service.UserSearchProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/users/{userId}/preferences")
@RequiredArgsConstructor
@Tag(name = "User Search Profile", description = "User search profile management endpoints")
public class UserSearchProfileController {

        private final UserSearchProfileService userSearchProfileService;
        private final EmploymentService employmentService;
        private final SkillService skillService;
        private final UserSearchProfileJobTitleService userSearchProfileJobTitleService;

        @GetMapping
        @Operation(summary = "Get user search profile", description = "Get the search profile for a specific user")
        public ApiResponse<UserSearchProfileResponse> getUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                userSearchProfileService.getUserSearchProfileByUserId(userId));
        }

        @GetMapping("/{searchProfileId}/skills")
        @Operation(summary = "Get user search profile skills", description = "Get all skills in a user's search profile")
        public ApiResponse<List<UserSearchProfileSkillResponse>> getUserSearchProfileSkills(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                skillService.getUserSearchProfileSkills(userId));
        }

        @GetMapping("/employments")
        @Operation(summary = "Get user search profile employments", description = "Get all employment types in a user's search profile")
        public ApiResponse<List<UserSearchProfileEmploymentResponse>> getUserSearchProfileEmployments(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                employmentService.getEmploymentStatusByUserId(userId));
        }

        @GetMapping("/titles")
        @Operation(summary = "Get user search profile job titles", description = "Get all job titles in a user's search profile")
        public ApiResponse<List<UserSearchProfileJobTitleResponse>> getUserSearchProfileJobTitles(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                userSearchProfileJobTitleService.getUserSearchProfileJobTitles(userId));
        }

        @PutMapping
        @Operation(summary = "Update user search profile", description = "Update the search profile for a specific user")
        public ApiResponse<UserSearchProfileResponse> updateUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @RequestBody UpdateSearchProfile request) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                userSearchProfileService.updateUserSearchProfile(userId, request));
        }

        @PostMapping
        @Operation(summary = "Create user search profile", description = "Create a search profile for a specific user")
        public ApiResponse<UserSearchProfileResponse> createUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @RequestBody CreateSearchProfile request) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                userSearchProfileService.createUserSearchProfile(request, userId));
        }

        @PostMapping("/{searchProfileId}/skills")
        @Operation(summary = "Add skill to user search profile", description = "Add a skill to a user's search profile")
        public ApiResponse<List<UserSearchProfileSkillResponse>> addSkillToUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @RequestParam("searchProfileId") UUID searchProfileId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @RequestBody CreateSearchProfileSkill request) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                skillService.addSkillToUserSearchProfile(request.getSkillIds(), searchProfileId));
        }

        @PostMapping("/{searchProfileId}/employments")
        @Operation(summary = "Add employment to user search profile", description = "Add an employment type to a user's search profile")
        public ApiResponse<List<UserSearchProfileEmploymentResponse>> addEmploymentToUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @RequestParam("searchProfileId") UUID searchProfileId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @RequestBody CreateSearchProfileEmployment request) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                employmentService.addEmployment(request, searchProfileId));
        }

        @PostMapping("/{searchProfileId}/titles")
        @Operation(summary = "Add job title to user search profile", description = "Add a job title to a user's search profile")
        public ApiResponse<List<UserSearchProfileJobTitleResponse>> addJobTitleToUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @RequestParam("searchProfileId") UUID searchProfileId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @RequestBody CreateSearchProfileJobTitle request) {
                authorize(userId, authUserId);
                return ApiResponse.success(
                                userSearchProfileJobTitleService.createUserSearchProfileJobTitle(request, userId));
        }

        @DeleteMapping("/{searchProfileId}/skills/{skillId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove skill from user search profile", description = "Remove a skill from a user's search profile")
        public ApiResponse<Void> removeSkillFromUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @Parameter(description = "Skill ID") @RequestParam UUID skillId) {
                authorize(userId, authUserId);
                skillService.removeSkillFromUserSearchProfile(userId, skillId);
                return ApiResponse.success("Skill removed from user search profile successfully", null);
        }

        @DeleteMapping("/{searchProfileId}/employments/{employmentId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove employment from user search profile", description = "Remove an employment type from a user's search profile")
        public ApiResponse<Void> removeEmploymentFromUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @Parameter(description = "Employment ID") @RequestParam UUID employmentId) {
                authorize(userId, authUserId);
                employmentService.removeEmploymentFromUserSearchProfile(userId, employmentId);
                return ApiResponse.success("Employment removed from user search profile successfully", null);
        }

        @DeleteMapping("/{searchProfileId}/titles/{jobTitleId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove job title from user search profile", description = "Remove a job title from a user's search profile")
        public ApiResponse<Void> removeJobTitleFromUserSearchProfile(
                        @RequestParam("userId") UUID userId,
                        @Parameter(description = "Authenticated User ID") @RequestHeader("X-User-Id") String authUserId,
                        @Parameter(description = "Job Title ID") @RequestParam UUID jobTitleId) {
                authorize(userId, authUserId);
                userSearchProfileJobTitleService.deleteUserSearchProfileJobTitle(userId, jobTitleId);
                return ApiResponse.success("Job title removed from user search profile successfully", null);
        }

        private void authorize(UUID userIdFromPath, String authUserIdStr) {
                UUID authUserId = UUID.fromString(authUserIdStr);
                if (!userIdFromPath.equals(authUserId)) {
                        throw new ForbiddenException(
                                        "You are not authorized to access this resource.");
                }
        }

}
