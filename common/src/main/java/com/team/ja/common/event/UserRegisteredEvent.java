package com.team.ja.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new user registers.
 * auth-service publishes this, user-service consumes it to create user profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent {

    /**
     * User ID - same ID will be used in user-service.
     */
    private UUID userId;

    /**
     * User's email address.
     */
    private String email;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * Timestamp when registration occurred.
     */
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();
}

