// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\request\UpdateApplicationStatusAdminRequest.java

package com.team.ja.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating application status (for admins).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update application status admin request")
public class UpdateApplicationStatusAdminRequest {

    @NotBlank(message = "Status is required")
    @Schema(description = "New application status (SUBMITTED, REVIEW, INTERVIEW, OFFERED, REJECTED, WITHDRAWN)", example = "REVIEW")
    private String status;

    @Schema(description = "Notes from admin", example = "Application is under review")
    private String adminNotes;
}
