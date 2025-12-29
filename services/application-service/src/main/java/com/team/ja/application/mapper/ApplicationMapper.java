// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\mapper\ApplicationMapper.java

package com.team.ja.application.mapper;

import com.team.ja.application.dto.response.ApplicationResponse;
import com.team.ja.application.model.JobApplication;
import com.team.ja.common.enumeration.DocType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting JobApplication entity to ApplicationResponse DTO.
 */
@Component
public class ApplicationMapper {

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
                .availableDocuments(buildAvailableDocuments(entity))
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
     * Build list of available documents for the application.
     */
    private List<DocType> buildAvailableDocuments(JobApplication entity) {
        List<DocType> availableDocs = new ArrayList<>();
        
        if (entity.getResumeUrl() != null && !entity.getResumeUrl().isEmpty()) {
            availableDocs.add(DocType.RESUME);
        }
        
        if (entity.getCoverLetterUrl() != null && !entity.getCoverLetterUrl().isEmpty()) {
            availableDocs.add(DocType.COVER_LETTER);
        }
        
        return availableDocs;
    }
}
