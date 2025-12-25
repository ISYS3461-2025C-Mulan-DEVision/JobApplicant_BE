package com.team.ja.subscription.model.search_profile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search profile entity.
 * Contains search profile information for user and support feature in
 * subscription service.
 */
@Entity
@Table(name = "search_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfile extends BaseEntity {

    /**
     * The ID of the user associated with this search profile
     */
    @Column(name = "user_id", nullable = false, unique = true)
    @Schema(description = "The ID of the user associated with this search profile")
    private UUID userId;

    /**
     * The ID of the country associated with this search profile
     */
    @Column(name = "country_id", nullable = true, unique = false)
    @Schema(description = "The ID of the country associated with this search profile")
    private UUID countryId;

    /**
     * The minimum salary for the search profile
     */
    @Column(name = "salary_min", nullable = true, unique = false)
    @Schema(description = "The minimum salary for the search profile")
    private BigDecimal salaryMin;

    /**
     * The maximum salary for the search profile
     */
    @Column(name = "salary_max", nullable = true, unique = false)
    @Schema(description = "The maximum salary for the search profile")
    private BigDecimal salaryMax;

    /**
     * The job title associated with this search profile
     */
    @Column(name = "job_title", nullable = true, unique = false)
    @Schema(description = "The job title associated with this search profile")
    private String jobTitle;

    /**
     * The skills associated with this search profile
     */
    @OneToMany(mappedBy = "searchProfile")
    @Schema(description = "The skills associated with this search profile")
    private List<SearchProfileSkill> skills;

    /**
     * The employment types (full-time, part-time, etc.) associated with this search
     * profile
     */
    @OneToMany(mappedBy = "searchProfile")
    @Schema(description = "The employments associated with this search profile")
    private List<SearchProfileEmployment> employments;

}
