package com.team.ja.admin.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.team.ja.admin.client.UserClient;
import com.team.ja.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/skills")
@RequiredArgsConstructor
@Tag(name = "Admin - Skill Management", description = "Endpoints for managing skills")
public class AdminSkillController {

    private final UserClient userClient;

    @GetMapping
    @Operation(summary = "Get all skills", description = "Retrieve all skills")
    public ApiResponse<Object> getAllSkills() {
        return userClient.getAllSkills();
    }

    @PostMapping
    @Operation(summary = "Create new skill", description = "Admin create new skill")
    public ApiResponse<Object> createSkill(String name) {
        return userClient.createSkill(name);
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a skill", description = "Search for a skill")
    public ApiResponse<Object> searchSkills(String query) {
        return userClient.searchSkills(query);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular skills", description = "Retrieve popular skills")
    public ApiResponse<Object> getPopularSkills() {
        return userClient.getPopularSkills();
    }

}
