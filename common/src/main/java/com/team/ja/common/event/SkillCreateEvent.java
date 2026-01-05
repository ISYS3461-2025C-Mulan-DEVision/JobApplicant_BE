package com.team.ja.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCreateEvent {

    /**
     * Unique ID for this specific event instance.
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * The ID of the skill that was created.
     * To sync skill catalogs across services.
     */
    private UUID skillId;

    /**
     * The name of the skill that was created.
     */
    private String name;

    /**
     * Normalized name of the skill that was created.
     */
    private String normalizedName;

    /**
     * Timestamp when the skill was created.
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
