package com.team.ja.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User response DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information")
public class UserResponse {

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's phone number", example = "+84901234567")
    private String phone;

    @Schema(description = "User's objective summary")
    private String objectiveSummary;

    @Schema(description = "Whether user has premium subscription")
    private boolean isPremium;

    @Schema(description = "Whether user account is active")
    private boolean isActive;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last profile update date")
    private LocalDateTime profileUpdatedAt;

    // Nested objects (populated via separate queries)
    @Schema(description = "User's country")
    private CountryResponse country;
}

