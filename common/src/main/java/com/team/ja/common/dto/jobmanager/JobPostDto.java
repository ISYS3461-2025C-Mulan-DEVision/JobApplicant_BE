package com.team.ja.common.dto.jobmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\dto\jobmanager\JobPostDto.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostDto {
    private UUID id;
    private UUID companyId;
    private String title;
    private String description;
    private String salaryType;
    private Double salaryMin;
    private Double salaryMax;
    private String salaryNote;
    private String locationCity;
    private String countryCode;
    private LocalDateTime postedAt;
    private LocalDateTime expiryAt;
    
    // Employment type can be a single value or array depending on endpoint
    private String employmentType;
    private List<String> employmentTypes;
    
    @JsonProperty("aprivate")
    private boolean aprivate;
    
    @JsonProperty("isFresher")
    private boolean isFresher;
    
    @JsonProperty("isPublished")
    private boolean isPublished;
    
    @JsonProperty("isPrivate")
    private boolean isPrivate;
}
