package com.team.ja.user.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.team.ja.common.enumeration.EmploymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkExperienceDto {

    private UUID id;
    private String jobTitle;
    private String companyName;
    private EmploymentType employmentType;
    private String countryAbbreviation;
    private LocalDate startAt;
    private LocalDate endAt;
    private boolean isCurrent;
    private String description;

}
