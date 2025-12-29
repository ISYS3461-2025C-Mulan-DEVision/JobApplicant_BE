package com.team.ja.subscription.model.search_profile;

import java.util.UUID;

import com.team.ja.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search profile job title entity.
 */
@Entity
@Table(name = "search_profile_job_titles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfileJobTitle extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    @Schema(description = "The user that associated with this job title")
    private UUID userId;

    @Column(name = "title", nullable = false)
    @Schema(description = "The job title text")
    private String title;

    @ManyToOne
    @JoinColumn(name = "search_profile_id", nullable = false)
    @Schema(description = "The search profile associated with this job title")
    private SearchProfile searchProfile;
}
