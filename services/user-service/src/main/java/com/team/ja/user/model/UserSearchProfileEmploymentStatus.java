package com.team.ja.user.model;

import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.EmploymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_search_profile_employment_statuses", uniqueConstraints = @UniqueConstraint(name = "sp_user_employment_status", columnNames = {
        "user_search_profile_id",
        "employment_type" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSearchProfileEmploymentStatus extends BaseEntity {

    /**
     * User Id associated with this skill table.
     */
    @Column(name = "user_search_profile_id", nullable = false)
    private UUID userSearchProfileId;

    @ManyToOne
    @JoinColumn(name = "user_search_profile_id", insertable = false, updatable = false)
    private UserSearchProfile userSearchProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

}
