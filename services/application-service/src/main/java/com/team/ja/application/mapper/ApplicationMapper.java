// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\mapper\ApplicationMapper.java

package com.team.ja.application.mapper;

import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.model.JobApplication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting JobApplication entity to ApplicationResponse DTO.
 */
@Component
public class ApplicationMapper {

    private final ObjectMapper objectMapper;

    public ApplicationMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert JobApplication entity to ApplicationResponse DTO.
     */
    public ApplicationResponse toResponse(JobApplication entity) {
        if (entity == null) {
            return null;
        }

        return ApplicationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .jobPostId(entity.getJobPostId())
                .status(entity.getStatus().toString())
                .resumeUrl(entity.getResumeUrl())
                .coverLetterUrl(entity.getCoverLetterUrl())
                .additionalFiles(parseAdditionalFiles(entity.getAdditionalFiles()))
                .appliedAt(entity.getAppliedAt())
                .applicationStatusUpdatedAt(entity.getApplicationStatusUpdatedAt())
                .userNotes(entity.getUserNotes())
                .adminNotes(entity.getAdminNotes())
                .companyUserNotes(entity.getCompanyUserNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.isDeleted())
                .build();
    }

    /**
     * Parse additional files JSON string to List.
     */
    private List<String> parseAdditionalFiles(String additionalFilesJson) {
        if (additionalFilesJson == null || additionalFilesJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(additionalFilesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Log error if needed
            return new ArrayList<>();
        }
    }

    /**
     * Convert additional files List to JSON string for storage.
     */
    public String serializeAdditionalFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(fileUrls);
        } catch (Exception e) {
            // Log error if needed
            return null;
        }
    }
}
