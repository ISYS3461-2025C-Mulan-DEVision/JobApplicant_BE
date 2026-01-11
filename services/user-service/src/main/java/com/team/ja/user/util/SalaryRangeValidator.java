package com.team.ja.user.util;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;

/**
 * Validator and utility class for salary range operations.
 * 
 * Rules:
 * - If minimum salary is not set, default to BigDecimal.ZERO
 * - If maximum salary is not set, treat as unlimited (no upper limit)
 * - Salary ranges should not have min > max
 */
@Slf4j
public class SalaryRangeValidator {

    private SalaryRangeValidator() {
        // Utility class
    }

    /**
     * Validates salary range and applies defaults.
     * 
     * @param minSalary minimum salary (can be null)
     * @param maxSalary maximum salary (can be null)
     * @return validated and normalized salary range
     * @throws IllegalArgumentException if validation fails
     */
    public static SalaryRange validateAndNormalize(BigDecimal minSalary, BigDecimal maxSalary) {
        // Apply defaults
        BigDecimal normalizedMin = minSalary != null ? minSalary : BigDecimal.ZERO;
        BigDecimal normalizedMax = maxSalary;

        // Validate that min is not negative
        if (normalizedMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum salary cannot be negative");
        }

        // Validate that max is not negative if provided
        if (normalizedMax != null && normalizedMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Maximum salary cannot be negative");
        }

        // Validate that min <= max
        if (normalizedMax != null && normalizedMin.compareTo(normalizedMax) > 0) {
            throw new IllegalArgumentException(
                    "Minimum salary (" + normalizedMin + ") cannot be greater than maximum salary ("
                            + normalizedMax + ")");
        }

        log.debug("Salary range validated: min={}, max={}", normalizedMin, normalizedMax);
        return new SalaryRange(normalizedMin, normalizedMax);
    }

    /**
     * Check if a job's salary overlaps with the search profile's salary range.
     * Includes jobs with undeclared/null salary.
     * 
     * @param jobSalaryMin     job's minimum salary (can be null)
     * @param jobSalaryMax     job's maximum salary (can be null)
     * @param profileMinSalary profile's minimum salary requirement
     * @param profileMaxSalary profile's maximum salary requirement (can be null for
     *                         no limit)
     * @return true if job salary is within profile's range OR has undeclared salary
     */
    public static boolean isSalaryInRange(BigDecimal jobSalaryMin, BigDecimal jobSalaryMax,
            BigDecimal profileMinSalary, BigDecimal profileMaxSalary) {

        // If job has undeclared salary, include it
        if ((jobSalaryMin == null || jobSalaryMin.compareTo(BigDecimal.ZERO) <= 0) && jobSalaryMax == null) {
            return true;
        }

        // If job has no minimum but has maximum, check against profile's range
        if (jobSalaryMin == null && jobSalaryMax != null) {
            if (profileMaxSalary == null) {
                // Profile has no max limit, job max is acceptable
                return jobSalaryMax.compareTo(profileMinSalary) >= 0;
            }
            return jobSalaryMax.compareTo(profileMinSalary) >= 0;
        }

        // If job has minimum, check against profile's max
        if (jobSalaryMin != null) {
            if (profileMaxSalary != null) {
                // Both have limits, check overlap
                return jobSalaryMin.compareTo(profileMaxSalary) <= 0
                        && (jobSalaryMax == null || jobSalaryMax.compareTo(profileMinSalary) >= 0);
            } else {
                // Profile has no max limit, only check job min against profile min
                return jobSalaryMin.compareTo(profileMinSalary) <= 0
                        || (jobSalaryMax != null && jobSalaryMax.compareTo(profileMinSalary) >= 0);
            }
        }

        return false;
    }

    /**
     * Represents a validated salary range.
     */
    public static class SalaryRange {
        public final BigDecimal min;
        public final BigDecimal max; // null means unlimited

        public SalaryRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return "SalaryRange [min=" + min + ", max=" + (max == null ? "unlimited" : max) + "]";
        }
    }
}
