package com.team.ja.common.dto.jobmanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResultDto {
    private UUID id;
    private UUID companyId;
    private String title;
    private String description;
    private String locationCity;
    private String countryCode;
    private String salaryType;
    private Double salaryMin;
    private Double salaryMax;
    private String salaryNote;
    private List<String> employmentTypes;
    private List<UUID> skillIds;
    private LocalDateTime postedAt;
    private LocalDateTime expiryAt;
    private boolean active;
    private boolean fresher;
}
