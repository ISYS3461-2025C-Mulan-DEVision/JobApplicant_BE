package com.team.ja.user.model;

import com.team.ja.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Skill reference entity.
 * Contains list of skills that users can add to their profile.
 */
@Entity
@Table(name = "skills", schema = "user_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Skill extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Lowercase, trimmed version of name for searching.
     */
    @Column(nullable = false)
    private String normalizedName;

    /**
     * How many users have this skill (for popularity/suggestions).
     */
    @Column(nullable = false)
    private int usageCount;
}
