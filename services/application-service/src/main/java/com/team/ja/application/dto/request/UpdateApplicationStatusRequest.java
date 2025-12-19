// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\request\UpdateApplicationStatusRequest.java

package com.team.ja.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating application status (for applicants).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update application status request")
public class UpdateApplicationStatusRequest {

    @NotBlank(message = "Status is required")
    @Schema(description = "New application status (SUBMITTED, REVIEW, INTERVIEW, OFFERED, REJECTED, WITHDRAWN)", example = "WITHDRAWN")
    private String status;

    @Schema(description = "Optional notes from applicant", example = "No longer interested in this position")
    private String notes;
}
