// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\services\application-service\src\main\java\com\team\ja\application\dto\response\ApplicationStatisticsResponse.java

package com.team.ja.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for application statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Application statistics response")
public class ApplicationStatisticsResponse {

    @Schema(description = "Total number of applications", example = "150")
    private Long totalApplications;

    @Schema(description = "Breakdown of applications by status")
    private Map<String, Long> applicationsByStatus;

    @Schema(description = "Number of applications submitted this month", example = "45")
    private Long applicationsThisMonth;

    @Schema(description = "Number of withdrawn applications", example = "12")
    private Long withdrawnApplications;

    @Schema(description = "Average time to first review in days", example = "2.5")
    private Double avgTimeToReview;
}
