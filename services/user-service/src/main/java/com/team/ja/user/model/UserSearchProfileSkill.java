package com.team.ja.user.model;

import java.util.UUID;

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

@Entity
@Table(name = "user_search_profile_skills", uniqueConstraints = @UniqueConstraint(name = "sp_user_skill", columnNames = {
        "user_search_profile_id",
        "skill_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSearchProfileSkill extends BaseEntity {

    /**
     * User Id associated with this skill table.
     */
    @Column(name = "user_search_profile_id", nullable = false)
    private UUID userSearchProfileId;

    @ManyToOne
    @JoinColumn(name = "user_search_profile_id", insertable = false, updatable = false)
    private UserSearchProfile userSearchProfile;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @ManyToOne
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;

}
