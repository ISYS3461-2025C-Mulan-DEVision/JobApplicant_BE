package com.team.ja.user.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.team.ja.common.enumeration.EmploymentType;

import lombok.extern.slf4j.Slf4j;

/**
 * Validator for employment type selections.
 * 
 * Rules:
 * - If neither FULL_TIME nor PART_TIME is selected, automatically add both
 * - All selections must be valid EmploymentType enum values
 * - Duplicate selections should be removed
 */
@Slf4j
public class EmploymentTypeValidator {

    private EmploymentTypeValidator() {
        // Utility class
    }

    /**
     * Validates and normalizes employment type selections.
     * If neither FULL_TIME nor PART_TIME is present, adds both.
     * 
     * @param employmentTypes list of employment types (can be null or empty)
     * @return normalized list of employment types
     * @throws IllegalArgumentException if validation fails
     */
    public static List<EmploymentType> validateAndNormalize(List<EmploymentType> employmentTypes) {
        Set<EmploymentType> normalized = new HashSet<>();

        if (employmentTypes != null) {
            // Remove nulls and validate
            for (EmploymentType type : employmentTypes) {
                if (type != null) {
                    normalized.add(type);
                }
            }
        }

        // Rule: If neither FULL_TIME nor PART_TIME, add both
        boolean hasFullTime = normalized.contains(EmploymentType.FULL_TIME);
        boolean hasPartTime = normalized.contains(EmploymentType.PART_TIME);

        if (!hasFullTime && !hasPartTime) {
            log.debug("Neither FULL_TIME nor PART_TIME selected. Adding both by default.");
            normalized.add(EmploymentType.FULL_TIME);
            normalized.add(EmploymentType.PART_TIME);
        }

        List<EmploymentType> result = new ArrayList<>(normalized);
        log.debug("Employment types normalized: {}", result);
        return result;
    }

    /**
     * Check if a job's employment type matches the search profile's preferences.
     * 
     * @param jobEmploymentTypes     job's employment types
     * @param profileEmploymentTypes profile's preferred employment types (after
     *                               normalization)
     * @return true if job has at least one matching employment type
     */
    public static boolean isEmploymentTypeMatch(List<EmploymentType> jobEmploymentTypes,
            List<EmploymentType> profileEmploymentTypes) {

        if (jobEmploymentTypes == null || jobEmploymentTypes.isEmpty()) {
            return false;
        }

        if (profileEmploymentTypes == null || profileEmploymentTypes.isEmpty()) {
            return false;
        }

        // Check if any job employment type is in the profile's preferences
        for (EmploymentType jobType : jobEmploymentTypes) {
            if (profileEmploymentTypes.contains(jobType)) {
                return true;
            }
        }

        return false;
    }
}
