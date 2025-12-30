package com.team.ja.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new user.
 * Note: In production, user creation will be triggered by auth-service
 * after successful registration. This DTO is used for internal service calls.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create user request")
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Pattern(
        regexp = "^\\+[0-9]{1,12}$",
        message = "Phone number must start with '+' followed by up to 12 digits."
    )
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "User's phone number", example = "+84901234567")
    private String phone;

    @NotNull(message = "Country is required")
    @Schema(description = "Country ID")
    private UUID countryId;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(
        description = "Street address (name/number)",
        example = "123 Nguyen Hue Street"
    )
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City name", example = "Ho Chi Minh City")
    private String city;

    @Size(
        max = 2000,
        message = "Objective summary must not exceed 2000 characters"
    )
    @Schema(description = "User's objective/summary")
    private String objectiveSummary;
}
