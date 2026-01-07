// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\response\ApplicationResponse.java

package com.team.ja.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team.ja.common.enumeration.DocType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for application details.
 * Used for returning application information to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Application response")
public class ApplicationResponse {

    @Schema(description = "Application ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User ID who submitted the application", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID userId;

    @Schema(description = "Job post ID for which application was submitted", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID jobPostId;

    @Schema(description = "Current application status", example = "SUBMITTED")
    private String status;

    @Schema(description = "Available document types in this application", example = "[\"RESUME\", \"COVER_LETTER\"]")
    private List<DocType> availableDocuments;

    @Schema(description = "Application submission timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appliedAt;

    @Schema(description = "Last status update timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applicationStatusUpdatedAt;

    @Schema(description = "Notes from applicant")
    private String userNotes;

    @Schema(description = "Notes from admin")
    private String adminNotes;

    @Schema(description = "Notes from company user (HR/Recruiter)")
    private String companyUserNotes;

    @Schema(description = "Record creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Is application soft deleted", example = "false")
    private Boolean deleted;
}
