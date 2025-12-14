package com.team.ja.user.dto.request;

import com.team.ja.common.enumeration.EducationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating user education entry.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Update user education request")
public class UpdateUserEducationRequest {

    @Size(max = 255, message = "Institution name must not exceed 255 characters")
    @Schema(description = "Institution name", example = "MIT")
    private String institution;

    @Schema(description = "Education level", example = "BACHELOR")
    private EducationLevel educationLevel;

    @Size(max = 255, message = "Field of study must not exceed 255 characters")
    @Schema(description = "Field of study", example = "Computer Science")
    private String fieldOfStudy;

    @Schema(description = "Start date", example = "2018-09-01")
    private LocalDate startAt;

    @Schema(description = "End date (null if ongoing)", example = "2022-06-01")
    private LocalDate endAt;
}
