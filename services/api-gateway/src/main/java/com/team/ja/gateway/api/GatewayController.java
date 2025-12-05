package com.team.ja.gateway.api;

import com.team.ja.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "api-gateway");
        status.put("status", "UP");
        return ApiResponse.success("API Gateway is running", status);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "api-gateway");
        info.put("version", "0.0.1");
        info.put("description", "API Gateway - Entry point for all requests");
        return ApiResponse.success(info);
    }

    @GetMapping("/services")
    public ApiResponse<Map<String, String>> services() {
        Map<String, String> services = new HashMap<>();
        services.put("auth-service", "http://localhost:8081");
        services.put("job-service", "http://localhost:8082");
        services.put("application-service", "http://localhost:8083");
        services.put("company-service", "http://localhost:8084");
        return ApiResponse.success("Available services", services);
    }

}

