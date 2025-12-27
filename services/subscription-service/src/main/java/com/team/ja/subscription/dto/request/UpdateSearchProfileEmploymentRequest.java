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
@Schema(description = "Update search profile employment request")
public class UpdateSearchProfileEmploymentRequest {

    @NotNull
    @Schema(description = "Employment type", example = "FULL_TIME")
    private EmploymentType employmentType;

}
