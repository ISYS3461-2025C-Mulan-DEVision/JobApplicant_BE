package com.team.ja.subscription.dto.request;

import com.team.ja.common.enumeration.EmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create search profile employment request")
public class CreateSearchProfileEmploymentRequest {

    @NotNull
    @Schema(description = "Employment type to add", example = "FULL_TIME")
    private EmploymentType employmentType;

}
