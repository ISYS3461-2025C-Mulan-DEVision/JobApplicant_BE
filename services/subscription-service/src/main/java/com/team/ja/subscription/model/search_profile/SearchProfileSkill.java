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
 * Search profile skill entity.
 * Contains search profile skill information for user and support feature in
 * subscription service.
 */
@Entity
@Table(name = "search_profile_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfileSkill extends BaseEntity {

    /**
     * The user that associated with this search profile skill
     */
    @Column(name = "user_id", nullable = false)
    @Schema(description = "The user that associated with this search profile skill")
    private UUID userId;

    /**
     * The skill associated with this search profile skill
     */
    @Column(name = "skill_id", nullable = false)
    @Schema(description = "The skill associated with this search profile skill")
    private UUID skillId;

    /**
     * The search profile associated with this skill
     */
    @ManyToOne
    @JoinColumn(name = "search_profile_id", nullable = false)
    @Schema(description = "The search profile associated with this skill")
    private SearchProfile searchProfile;
}
