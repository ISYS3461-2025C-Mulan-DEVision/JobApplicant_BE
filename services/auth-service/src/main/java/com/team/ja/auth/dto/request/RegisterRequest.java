package com.team.ja.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration request DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
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
        description = "User's password (min 8 characters, 1 uppercase, 1 number, 1 special char)",
        example = "SecureP@ss123"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Country is required")
    @Size(
        min = 2,
        max = 3,
        message = "Country abbreviation must be 2-3 characters"
    )
    @Schema(description = "Country abbreviation (2-letter)", example = "US")
    private String country;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "Phone number", example = "+84123456789")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(
        description = "Street address (name/number)",
        example = "123 Nguyen Hue Street"
    )
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City name", example = "Ho Chi Minh City")
    private String city;
}
