package com.team.ja.user.model;

import java.util.UUID;

import org.checkerframework.checker.units.qual.C;

import com.team.ja.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_search_profile_job_titles", uniqueConstraints = @UniqueConstraint(name = "sp_user_job_title", columnNames = {
        "user_search_profile_id",
        "job_title" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSearchProfileJobTitle extends BaseEntity {

    /**
     * User Id associated with this job title table.
     */
    @Column(name = "user_search_profile_id", nullable = false)
    private UUID userSearchProfileId;

    /**
     * User associated with this job title table.
     */
    @ManyToOne
    @JoinColumn(name = "user_search_profile_id", insertable = false, updatable = false)
    private UserSearchProfile userSearchProfile;

    /**
     * Job Title associated with this job title table.
     */
    @Column(name = "job_title", nullable = false)
    private String jobTitle;

}
