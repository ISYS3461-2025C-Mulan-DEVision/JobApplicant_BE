package com.team.ja.user.model;

import com.team.ja.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Column(columnDefinition = "TEXT")
    private String searchVector;

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
