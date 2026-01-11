package com.team.ja.admin.client;

import com.team.ja.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "application-service")
public interface ApplicationClient {

    @GetMapping("/api/v1/admin/applications")
    ApiResponse<Object> getAllApplications(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "jobPostId", required = false) UUID jobPostId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size);

    @GetMapping("/api/v1/admin/applications/{applicationId}")
    ApiResponse<Object> getApplicationById(@PathVariable("applicationId") UUID applicationId);

    @PatchMapping("/api/v1/admin/applications/{applicationId}/status")
    ApiResponse<Object> updateApplicationStatus(
            @PathVariable("applicationId") UUID applicationId,
            @RequestBody Object request);

    @DeleteMapping("/api/v1/admin/applications/{applicationId}")
    ApiResponse<Void> deleteApplication(@PathVariable("applicationId") UUID applicationId);

    @GetMapping("/api/v1/admin/applications/statistics")
    ApiResponse<Object> getApplicationStatistics(
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) LocalDateTime endDate);
    
    @PatchMapping("/api/v1/admin/applications/bulk/status")
    ApiResponse<String> bulkUpdateApplicationStatus(@RequestBody Object request);
}
