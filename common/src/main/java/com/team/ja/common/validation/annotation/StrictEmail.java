package com.team.ja.common.validation.annotation;

import com.team.ja.common.validation.validator.StrictEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation for strict email validation based on Requirement 1.2.2.
 * 
 * Rules:
 * 1. Exactly one '@' symbol.
 * 2. At least one '.' (dot) after the '@' symbol.
 * 3. Total length < 255 characters.
 * 4. No spaces or prohibited characters: ( ) [ ] ; :
 */
@Documented
@Constraint(validatedBy = StrictEmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrictEmail {
    String message() default "Invalid email format. Must contain exactly one '@', at least one '.' after '@', no spaces or prohibited characters like ( ) [ ] ; : and be less than 255 chars.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
