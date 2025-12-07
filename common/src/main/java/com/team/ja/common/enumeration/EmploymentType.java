package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Employment type enumeration.
 * Used to categorize job positions and work experience.
 */
@Getter
@RequiredArgsConstructor
public enum EmploymentType {

    FULL_TIME("Full-time"),
    PART_TIME("Part-time"),
    CONTRACT("Contract"),
    INTERNSHIP("Internship"),
    FREELANCE("Freelance"),
    TEMPORARY("Temporary"),
    VOLUNTEER("Volunteer");

    private final String displayName;
}

