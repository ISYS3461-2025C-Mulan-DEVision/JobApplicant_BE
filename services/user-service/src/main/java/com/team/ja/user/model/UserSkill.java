package com.team.ja.user.model;

import com.team.ja.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * User-Skill junction entity.
 * Links users to their skills (many-to-many relationship).
 */
@Entity
@Table(
    name = "user_skills",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_skill",
        columnNames = {"user_id", "skill_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSkill extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @ManyToOne
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;
}

