package com.team.ja.admin.api;

import com.team.ja.admin.client.ApplicationClient;
import com.team.ja.admin.client.JobManagerClient;
import com.team.ja.admin.client.UserClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Global Search", description = "Unified search across the system")
public class AdminSearchController {

    private final UserClient userClient;
    private final JobManagerClient jobManagerClient;
    // private final ApplicationClient applicationClient; // Applications usually
    // searched by filters, not global text

    @GetMapping
    @Operation(summary = "Global search", description = "Search for Applicants, Companies, and Job Posts by name/title")
    public ApiResponse<GlobalSearchResult> globalSearch(
            @RequestParam String query) {

        if (query == null || query.trim().isEmpty()) {
            return ApiResponse.success(new GlobalSearchResult());
        }

        // Execute searches in parallel
        CompletableFuture<PageResponse<Object>> usersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // Search users by username (first/last name)
                return userClient.searchUsers(null, null, null, null, null, null, query, 0, 5).getData();
            } catch (Exception e) {
                log.error("Error searching users", e);
                return PageResponse.<Object>builder().content(java.util.Collections.emptyList()).build();
            }
        });

        CompletableFuture<PageResponse<Object>> companiesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return jobManagerClient.searchCompanies(query, 0, 5).getData();
            } catch (Exception e) {
                log.error("Error searching companies", e);
                return PageResponse.<Object>builder().content(java.util.Collections.emptyList()).build();
            }
        });

        CompletableFuture<Object> skillFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return userClient.searchSkills(query).getData();
            } catch (Exception e) {
                log.error("Error searching skills", e);
                return null;
            }
        });

        CompletableFuture<PageResponse<Object>> jobsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return jobManagerClient.searchJobPosts(query, null, 0, 5).getData();
            } catch (Exception e) {
                log.error("Error searching job posts", e);
                return PageResponse.<Object>builder().content(java.util.Collections.emptyList()).build();
            }
        });

        try {
            CompletableFuture.allOf(usersFuture, companiesFuture, jobsFuture, skillFuture).join();

            GlobalSearchResult result = GlobalSearchResult.builder()
                    .applicants(usersFuture.get())
                    .companies(companiesFuture.get())
                    .jobPosts(jobsFuture.get())
                    .skills(skillFuture.get())
                    .build();

            return ApiResponse.success(result);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Global search failed", e);
            return ApiResponse.error("Search failed: " + e.getMessage());
        }
    }

    @Data
    @Builder
    public static class GlobalSearchResult {
        private PageResponse<Object> applicants;
        private PageResponse<Object> companies;
        private PageResponse<Object> jobPosts;
        private Object skills;

        public GlobalSearchResult() {
        }

        public GlobalSearchResult(PageResponse<Object> applicants, PageResponse<Object> companies,
                PageResponse<Object> jobPosts, Object skills) {
            this.applicants = applicants;
            this.companies = companies;
            this.jobPosts = jobPosts;
            this.skills = skills;
        }
    }
}
