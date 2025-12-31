package com.team.ja.common.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.team.ja.common.enumeration.EmploymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a user updates their search profile.
 * 
 * This will link directly to JM applicant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchProfileUpdateEvent {

    /**
     * Unique ID for this specific event instance.
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * ID of the user who updated their search profile.
     */
    private UUID userId;

    /**
     * Type of update made to the search profile.
     */
    private UpdateType updateType;

    /**
     * The user's new country ID. Only present if updateType is COUNTRY.
     */
    private UUID countryId;

    /**
     * The user's minimum salary preference. Only present if updateType is
     * SALARY_RANGE.
     */
    private Double minSalary;

    /**
     * The user's maximum salary preference. Only present if updateType is
     * SALARY_RANGE.
     */
    private Double maxSalary;

    /**
     * The user's complete list of skill IDs. Only present if updateType is SKILLS.
     */
    private List<UUID> skillIds;

    /**
     * The user's complete list of employment types. Only present if updateType is
     * EMPLOYMENTS.
     */
    private List<EmploymentType> employmentTypes;

    /**
     * Timestamp of when the update occurred.
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Enum to specify what part of the search profile was updated.
     */
    public enum UpdateType {
        COUNTRY,
        SKILLS,
        EMPLOYMENTS,
        SALARY_RANGE
    }

}
