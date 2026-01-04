package com.team.ja.user.dto.response;

import com.team.ja.common.enumeration.EducationLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * User education response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User education information")
public class UserEducationResponse {

    @Schema(description = "Education record ID")
    private UUID id;

    @Schema(description = "Institution name", example = "MIT")
    private String institution;

    @Schema(description = "Education level")
    private EducationLevel educationLevel;

    @Schema(description = "Education level display name", example = "Bachelor's Degree")
    private String educationLevelDisplayName;

    @Schema(description = "Field of study", example = "Computer Science")
    private String fieldOfStudy;

    @Schema(description = "Degree name", example = "Bachelor of Software Engineering (Hons)")
    private String degree;

    @Schema(description = "GPA", example = "85.5")
    private Double gpa;

    @Schema(description = "Start date")
    private LocalDate startAt;

    @Schema(description = "End date (null if ongoing)")
    private LocalDate endAt;
}

