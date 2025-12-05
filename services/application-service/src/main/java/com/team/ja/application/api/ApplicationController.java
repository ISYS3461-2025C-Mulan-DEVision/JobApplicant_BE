package com.team.ja.application.api;

import com.team.ja.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "application-service");
        status.put("status", "UP");
        return ApiResponse.success("Application Service is running", status);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "application-service");
        info.put("version", "0.0.1");
        info.put("description", "Job Application Management Service");
        return ApiResponse.success(info);
    }

}

