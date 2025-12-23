package com.team.ja.admin.api;

import com.team.ja.admin.kafka.kafka_producer.KafkaRequest;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.SkillResponse;
import com.team.ja.common.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrator management endpoints")
public class AdminController {

    private final KafkaRequest kafkaRequest;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the admin service is running")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "admin-service");
        status.put("status", "UP");
        return ApiResponse.success("Admin Service is running", status);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get admin service information")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "admin-service");
        info.put("version", "0.0.1");
        info.put("description", "Administrator Management Service");
        info.put("handles", "Administrator");
        return ApiResponse.success(info);
    }

    @GetMapping("/applicants")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all applicants", description = "Retrieve a list of all job applicants")
    public ApiResponse<List<UserResponse>> getAllApplicants() throws Exception {
        List<UserResponse> users = kafkaRequest.sendUserDataRequest("Requesting all user data");
        return ApiResponse.success(users);
    }

    @GetMapping("/skills")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all skills", description = "Retrieve a list of all skills")
    public ApiResponse<List<SkillResponse>> getAllSkills() throws Exception {
        List<SkillResponse> skills = kafkaRequest.sendSkillDataRequest("Requesting all skill data");
        return ApiResponse.success(skills);
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create skill", description = "Create a new skill")
    public ApiResponse<SkillResponse> createSkill(@Parameter(description = "Skill name") @RequestParam String skillName)
            throws Exception {
        SkillResponse createdSkill = kafkaRequest.createSkillRequest(skillName);
        return ApiResponse.success("Skill created successfully", createdSkill);
    }

    @DeleteMapping("/applicants/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete applicant", description = "Deactivate a job applicant by user ID")
    public ApiResponse<String> deleteApplicant(@PathVariable UUID userId) throws Exception {
        String result = kafkaRequest.deleteUserRequest(userId);
        return ApiResponse.success(result);
    }

}
