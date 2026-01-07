package com.team.ja.user.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.team.ja.common.enumeration.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEducationDto {

    private UUID id;
    private String institution;
    private EducationLevel educationLevel;
    private String fieldOfStudy;
    private String degree;
    private Double gpa;
    private LocalDate startAt;
    private LocalDate endAt;

}
