package com.team.ja.user.model;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.EmploymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * User work experience entity.
 * Stores professional work history of a user.
 */
@Entity
@Table(name = "user_work_experience", schema = "user_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserWorkExperience extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    @Column(columnDefinition = "TEXT")
    private String description;
}

