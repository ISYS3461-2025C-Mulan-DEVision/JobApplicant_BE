package com.team.ja.subscription.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;
import com.team.ja.subscription.dto.response.SearchProfileEmployment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfileResponse {
    private UUID id;
    private UUID userId;
    private UUID countryId;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String jobTitle;
    private List<SearchProfileSkillResponse> skills;
    private List<SearchProfileEmployment> employments;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
