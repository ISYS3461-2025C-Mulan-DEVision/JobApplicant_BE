package com.team.ja.user.model;

import com.team.ja.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * User entity representing a job seeker profile.
 *
 * Inherits from BaseEntity:
 * - id, createdAt, updatedAt
 * - isActive, deactivatedAt (for soft delete)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String phone;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String objectiveSummary;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean isPremium = false;

    @Column
    private LocalDateTime profileUpdatedAt;

    /**
     * Full-text search vector for efficient searching.
     */
    @Column(
        name = "fts_document",
        columnDefinition = "tsvector",
        insertable = false,
        updatable = false
    )
    private String ftsDocument;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<UserSkill> userSkills = new HashSet<>();

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<UserPortfolioItem> portfolioItems = new HashSet<>();

    /**
     * Get user's full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Update the profile timestamp.
     */
    public void markProfileUpdated() {
        this.profileUpdatedAt = LocalDateTime.now();
    }
}
