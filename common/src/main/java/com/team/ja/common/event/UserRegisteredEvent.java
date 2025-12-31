package com.team.ja.common.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * Country abbreviation (2-letter), mandatory at registration.
     */
    private String countryAbbreviation;

    /**
     * Optional phone number.
     */
    private String phone;

    /**
     * Optional street address (name/number).
     */
    private String address;

    /**
     * Optional city name.
     */
    private String city;

    /**
     * Timestamp when registration occurred.
     */
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();
}
