package com.team.ja.subscription.model.search_profile;

import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;
import com.team.ja.common.enumeration.EmploymentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search profile employment entity.
 * Contains search profile employment type information for user and support
 * feature in subscription service.
 */
@Entity
@Table(name = "search_profile_employments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfileEmployment extends BaseEntity {

    /**
     * The user that associated with this search profile employment
     */
    @Column(name = "user_id", nullable = false)
    @Schema(description = "The user that associated with this search profile employment")
    private UUID userId;

    /**
     * The employment type associated with this search profile employment
     */
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    @Schema(description = "The employment type associated with this search profile employment")
    private EmploymentType employmentType;

    /**
     * The search profile associated with this employment
     */
    @ManyToOne
    @JoinColumn(name = "search_profile_id", nullable = false)
    @Schema(description = "The search profile associated with this employment")
    private SearchProfile searchProfile;

}
