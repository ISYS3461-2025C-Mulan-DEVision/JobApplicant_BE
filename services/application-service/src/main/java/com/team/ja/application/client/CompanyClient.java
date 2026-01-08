package com.team.ja.application.client;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.dto.jobmanager.CompanyDto;
import com.team.ja.common.dto.jobmanager.CompanyProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "${services.jm-company.name:JM_COMPANY}")
public interface CompanyClient {

    @GetMapping("/api/companies/{id}")
    ApiResponse<CompanyDto> getCompanyById(@PathVariable("id") UUID id);

    @GetMapping("/api/companies/{id}/profile")
    ApiResponse<CompanyProfileDto> getCompanyProfileById(@PathVariable("id") UUID id);
}
