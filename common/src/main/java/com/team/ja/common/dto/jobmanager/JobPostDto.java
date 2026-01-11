// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\dto\jobmanager\JobPostDto.java
package com.team.ja.common.dto.jobmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostDto {
    private UUID id;
    private UUID companyId;
    private String title;
    private String description;
    private String locationCity;
    private String countryCode;
    private String salaryType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryNote;
    private List<String> employmentTypes;
    private List<UUID> skillIds;
    private LocalDateTime postedAt;
    private LocalDateTime expiryAt;
    
    @JsonProperty("isActive")
    private boolean active;
    
    @JsonProperty("isFresher")
    private boolean fresher;
}
