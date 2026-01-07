// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\request\CreateApplicationRequest.java

package com.team.ja.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Request DTO for creating a new job application.
 * Includes file uploads for resume, cover letter, and optional additional files.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create application request")
public class CreateApplicationRequest {

    @NotNull(message = "Job post ID is required")
    @Schema(description = "ID of the job post to apply for", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID jobPostId;

    @NotNull(message = "Resume file is required")
    @Schema(description = "Resume file (PDF, DOC, DOCX)")
    private MultipartFile resumeFile;

    @NotNull(message = "Cover letter file is required")
    @Schema(description = "Cover letter file (PDF, DOC, DOCX)")
    private MultipartFile coverLetterFile;
}
