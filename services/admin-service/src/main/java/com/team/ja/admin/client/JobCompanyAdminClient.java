// File path: services/admin-service/src/main/java/com/team/ja/admin/client/JobCompanyAdminClient.java
package com.team.ja.admin.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.PageResponse;
import com.team.ja.admin.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for Job Manager - Company Service (Admin Operations)
 * Uses direct URL configuration with Bearer token authentication.
 * 
 * This client provides admin-level access to company management operations.
 */
@FeignClient(name = "${services.jm-company.name}", url = "${services.jm-company.url}", configuration = FeignConfig.class)
public interface JobCompanyAdminClient {

    /**
     * Get all companies with pagination and sorting
     * Note: Job Manager wraps response in ApiResponse
     * Note: This endpoint does NOT support filtering by name - returns all
     * companies
     * 
     * @param page      Page number (0-indexed)
     * @param size      Page size
     * @param sortBy    Sort field (default: "name")
     * @param direction Sort direction (ASC/DESC)
     * @return ApiResponse wrapping PageResponse of companies
     */
    @GetMapping("/api/companies")
    ApiResponse<PageResponse<Object>> getCompanies(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction);
}
