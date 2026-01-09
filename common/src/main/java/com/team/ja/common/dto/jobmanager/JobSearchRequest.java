package com.team.ja.common.dto.jobmanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchRequest {
    private String title;
    private List<String> employmentTypes;
    private String locationCity;
    private String countryCode;
    private Double minSalary;
    private Double maxSalary;
    private Boolean fresher;
    private Integer page;
    private Integer size;
}
