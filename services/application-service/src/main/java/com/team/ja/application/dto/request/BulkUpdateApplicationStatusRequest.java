// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\request\BulkUpdateApplicationStatusRequest.java

package com.team.ja.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk updating application statuses (admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bulk update application status request")
public class BulkUpdateApplicationStatusRequest {

    @NotEmpty(message = "Application IDs are required")
    @Schema(description = "List of application IDs to update")
    private List<UUID> applicationIds;

    @NotBlank(message = "Status is required")
    @Schema(description = "New status for all applications", example = "REJECTED")
    private String status;

    @Schema(description = "Admin notes for the bulk update", example = "Position has been filled")
    private String adminNotes;
}
