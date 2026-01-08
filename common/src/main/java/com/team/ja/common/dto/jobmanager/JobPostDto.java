package com.team.ja.common.dto.jobmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private String employmentType;
    
    @JsonProperty("aprivate")
    private boolean aprivate;
    
    @JsonProperty("isFresher")
    private boolean isFresher;
    
    @JsonProperty("isPublished")
    private boolean isPublished;
    
    @JsonProperty("isPrivate")
    private boolean isPrivate;
}
