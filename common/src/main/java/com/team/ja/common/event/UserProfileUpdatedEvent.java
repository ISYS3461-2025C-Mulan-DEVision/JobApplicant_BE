package com.team.ja.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a user's profile is updated in a way that is
 * relevant to other services (e.g., job matching).
 *
 * Producer: user-service
 * Consumer: notification-service (and potentially others)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdatedEvent {

    /**
     * Unique ID for this specific event instance.
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * The ID of the user whose profile was updated.
     */
    private UUID userId;

    /**
     * The type of update that occurred.
     */
    private UpdateType updateType;

    /**
     * The user's new country ID. Only present if updateType is COUNTRY.
     */
    private UUID countryId;

    /**
     * The user's complete list of skill IDs. Only present if updateType is SKILLS.
     */
    private List<UUID> skillIds;

    /**
     * Timestamp when the update occurred.
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Enum to specify what part of the profile was updated.
     */
    public enum UpdateType {
        SKILLS,
        COUNTRY
    }
}
