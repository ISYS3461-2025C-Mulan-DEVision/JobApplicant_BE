package com.team.ja.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMigrationEvent {

    // Identity
    private UUID userId;

    // Routing Information
    private String sourceShardId;
    private String targetShardId;

    private String newCountryAbbreviation;

    private LocalDateTime requestedAt;
    private String correlationId;

}
