package com.team.ja.admin.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

        @GetMapping("/api/v1/users")
        ApiResponse<PageResponse<Object>> getAllUsers(
                        @RequestParam("page") int page,
                        @RequestParam("size") int size);

        @GetMapping("/api/v1/users/search")
        ApiResponse<PageResponse<Object>> searchUsers(
                        @RequestParam(value = "skills", required = false) String skills,
                        @RequestParam(value = "country", required = false) String country,
                        @RequestParam(value = "city", required = false) String city,
                        @RequestParam(value = "education", required = false) String education,
                        @RequestParam(value = "workExperience", required = false) String workExperience,
                        @RequestParam(value = "employmentTypes", required = false) String employmentTypes,
                        @RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size);

        @GetMapping("/api/v1/users/{id}")
        ApiResponse<Object> getUserById(@PathVariable("id") UUID id);

        @DeleteMapping("/api/v1/users/{id}")
        ApiResponse<Void> deactivateUser(@PathVariable("id") UUID id, @RequestHeader("X-User-Id") String adminId);

        @PostMapping("/api/v1/users/{id}/reactivate")
        ApiResponse<Object> reactivateUser(@PathVariable("id") UUID id);

        @PostMapping("/api/v1/skills")
        ApiResponse<Object> createSkill(@RequestParam("name") String name);

        @GetMapping("/api/v1/skills")
        ApiResponse<Object> getAllSkills();

        @GetMapping("/api/v1/skills/{id}")
        ApiResponse<Object> getSkillById(@PathVariable("id") Long id);

        @GetMapping("/api/v1/skills/search")
        ApiResponse<Object> searchSkills(@RequestParam("q") String query);

        @GetMapping("/api/v1/skills/popular")
        ApiResponse<Object> getPopularSkills();

}
