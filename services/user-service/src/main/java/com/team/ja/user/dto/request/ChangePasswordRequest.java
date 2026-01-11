package com.team.ja.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change password request DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "User's current password", example = "OldP@ss123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(
        min = 8,
        max = 100,
        message = "Password must be between 8 and 100 characters"
    )
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[$#@!]).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character ($#@!)"
    )
    @Schema(
        description = "User's new password (min 8 characters, 1 uppercase, 1 number, 1 special char)",
        example = "NewP@ss123"
    )
    private String newPassword;
}

