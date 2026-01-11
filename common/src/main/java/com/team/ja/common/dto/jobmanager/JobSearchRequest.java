// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\dto\jobmanager\JobSearchRequest.java
package com.team.ja.common.dto.jobmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for searching job posts in Job Manager service.
 * All fields are optional and can be combined for filtering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchRequest {
    
    private String title;
    private List<String> employmentTypes;
    private String locationCity;
    private String countryCode;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Boolean fresher;
    
    // Pagination
    @JsonProperty("page")
    private Integer page = 0;
    
    @JsonProperty("size")
    private Integer size = 10;
}
