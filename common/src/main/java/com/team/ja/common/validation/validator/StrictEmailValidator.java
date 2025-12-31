package com.team.ja.common.validation.validator;

import com.team.ja.common.validation.annotation.StrictEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator logic for @StrictEmail.
 */
public class StrictEmailValidator implements ConstraintValidator<StrictEmail, String> {

    // Regex explanation:
    // ^ - Start of string
    // [^@\s()[\];:]+ - Part before @: 1+ chars, NOT @, whitespace, ( ) [ ] ; :
    // @ - Literal @
    // [^@\s()[\];:]+ - Part after @ (domain): 1+ chars, same exclusions
    // \. - Literal .
    // [^@\s()[\];:]+ - Part after . (TLD): 1+ chars, same exclusions
    // $ - End of string
    private static final String EMAIL_PATTERN = "^[^@\\s()\\[\\];:]+@[^@\\s()\\[\\];:]+\\.[^@\\s()\\[\\];:]+$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            // @NotNull or @NotBlank should handle null/empty checks on the field itself
            return true;
        }

        // Rule: Length < 255 characters
        if (email.length() >= 255) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email length must be less than 255 characters")
                    .addConstraintViolation();
            return false;
        }

        // Rule: Syntax (regex matches requirements)
        if (!PATTERN.matcher(email).matches()) {
            return false;
        }

        return true;
    }
}
