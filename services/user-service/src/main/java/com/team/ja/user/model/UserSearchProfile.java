package com.team.ja.user.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.EducationLevel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users_search_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSearchProfile extends BaseEntity {

    /**
     * The ID of the user associated with this search profile.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * Minimum desired salary for the search profile.
     */
    private BigDecimal salaryMin;

    /**
     * Maximum desired salary for the search profile.
     */
    private BigDecimal salaryMax;

    /**
     * The Country associated with this search profile.
     * Currently support 1 country only.
     * Use country abbreviation for easier querying.
     */
    private String countryAbbreviation;

    /**
     * Skill associated with this search profile.
     */
    @OneToMany(mappedBy = "userSearchProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserSearchProfileSkill> skills = new HashSet<>();

    /**
     * Employment status associated with this search profile.
     */
    @OneToMany(mappedBy = "userSearchProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserSearchProfileEmploymentStatus> employmentStatus = new HashSet<>();

    /**
     * Job title associated with this search profile.
     */
    @OneToMany(mappedBy = "userSearchProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSearchProfileJobTitle> jobTitle = new ArrayList<>();

    /**
     * Education level associated with this search profile.
     * Only one education level is allowed.
     * TODO: If possible, automatically fetch the highest education level from
     * UserEducation entity.
     */
    private EducationLevel educationLevel;

}
