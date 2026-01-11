// File path: services/admin-service/src/main/java/com/team/ja/admin/api/AdminCompanyController.java
package com.team.ja.admin.api;

import com.team.ja.admin.client.JobCompanyAdminClient;
import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing companies through Job Manager service.
 * Provides admin-level access to search and view companies.
 * 
 * Base Path: /api/v1/admin/companies
 */
@RestController
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Companies", description = "Admin endpoints for company management")
public class AdminCompanyController {

    private final JobCompanyAdminClient jobCompanyAdminClient;

    /**
     * Get all companies with pagination.
     * Calls Job Manager service to retrieve company list.
     * Note: Does NOT support filtering by name - returns all companies
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of companies wrapped in ApiResponse
     */
    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieve all companies with pagination")
    public ResponseEntity<ApiResponse<PageResponse<Object>>> getCompanies(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("Admin retrieving companies - page: {}, size: {}", page, size);

        try {
            // Call Job Manager service to get companies (returns ApiResponse wrapper)
            ApiResponse<PageResponse<Object>> response = jobCompanyAdminClient.getCompanies(page, size, "name", "ASC");

            if (response.isSuccess() && response.getData() != null) {
                log.info("Companies retrieved successfully: {} total items", response.getData().getTotalElements());
                return ResponseEntity.ok(ApiResponse.success("Companies retrieved successfully", response.getData()));
            } else {
                log.warn("Failed to retrieve companies: {}", response.getMessage());
                return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("Error retrieving companies: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve companies: " + e.getMessage())
            );
        }
    }
}
