package com.team.ja.user.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.team.ja.common.event.JobPostingEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.user.util.EmploymentTypeValidator;
import com.team.ja.user.util.SalaryRangeValidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating if a job posting matches a search profile.
 * 
 * Matching rules:
 * 1. Country: Must match or profile has no country restriction
 * 2. Employment Type: Must match or apply default logic (if no FT/PT selected,
 * both included)
 * 3. Salary: Must be within range or job has undeclared salary
 * 4. Skills: If profile has skills, job must have at least one overlapping
 * skill
 * 5. Fresher: If job is not fresher-friendly, profile must not require fresher
 * jobs
 * 6. Job Title: If profile has specific job titles, job title must match
 * (partial match)
 */
@Slf4j
@Service
public class JobMatchingService {

    /**
     * Evaluates if a job posting matches a search profile based on all criteria.
     * 
     * @param jobEvent     the job posting event
     * @param profileEvent the user's search profile
     * @return true if job matches all profile criteria, false otherwise
     */
    public boolean isMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        log.debug("Evaluating match for job {} against profile {}", jobEvent.getJobPostId(),
                profileEvent.getUserId());

        // Check country
        if (!isCountryMatch(jobEvent, profileEvent)) {
            log.debug("Country mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        // Check employment type
        if (!isEmploymentTypeMatch(jobEvent, profileEvent)) {
            log.debug("Employment type mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        // Check salary range
        if (!isSalaryMatch(jobEvent, profileEvent)) {
            log.debug("Salary mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        // Check skills
        if (!isSkillMatch(jobEvent, profileEvent)) {
            log.debug("Skill mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        // Check fresher requirement
        if (!isFresherMatch(jobEvent, profileEvent)) {
            log.debug("Fresher requirement mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        // Check job title
        if (!isJobTitleMatch(jobEvent, profileEvent)) {
            log.debug("Job title mismatch for job {} and profile {}", jobEvent.getJobPostId(),
                    profileEvent.getUserId());
            return false;
        }

        log.info("Job {} matches profile {}", jobEvent.getJobPostId(), profileEvent.getUserId());
        return true;
    }

    /**
     * Check if country matches.
     * Match if:
     * - Profile has no country restriction (null)
     * - Profile country matches job country
     */
    private boolean isCountryMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        // If profile has no country restriction, match any country
        if (profileEvent.getCountryAbbreviation() == null || profileEvent.getCountryAbbreviation().isEmpty()) {
            return true;
        }

        // If job has no country, don't match (be strict)
        if (jobEvent.getCountryCode() == null || jobEvent.getCountryCode().isEmpty()) {
            return false;
        }

        return profileEvent.getCountryAbbreviation().equalsIgnoreCase(jobEvent.getCountryCode());
    }

    /**
     * Check if employment type matches.
     * 
     * Logic:
     * - If job has employment types that overlap with profile's preferences, it's a
     * match
     * - If profile has no employment types selected (should not happen after
     * normalization),
     * it defaults to accepting both FULL_TIME and PART_TIME
     */
    private boolean isEmploymentTypeMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        if (jobEvent.getEmploymentTypes() == null || jobEvent.getEmploymentTypes().isEmpty()) {
            return false;
        }

        if (profileEvent.getEmploymentTypes() == null || profileEvent.getEmploymentTypes().isEmpty()) {
            // Default: accept FULL_TIME and PART_TIME
            return jobEvent.getEmploymentTypes().contains("FULL_TIME")
                    || jobEvent.getEmploymentTypes().contains("PART_TIME");
        }

        // Check for overlap
        for (String jobType : jobEvent.getEmploymentTypes()) {
            if (profileEvent.getEmploymentTypes().contains(jobType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if salary is within range.
     * 
     * Logic:
     * - If job has undeclared salary (null min and max), include it
     * - Otherwise check if job salary overlaps with profile's expected range
     */
    private boolean isSalaryMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        return SalaryRangeValidator.isSalaryInRange(jobEvent.getSalaryMin(), jobEvent.getSalaryMax(),
                profileEvent.getMinSalary() != null ? profileEvent.getMinSalary() : BigDecimal.ZERO,
                profileEvent.getMaxSalary());
    }

    /**
     * Check if skills match.
     * 
     * Logic:
     * - If profile has no skill requirements, match any job
     * - If profile has skills, job must have at least one overlapping skill
     */
    private boolean isSkillMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        // If profile has no skill requirements, match
        if (profileEvent.getSkillIds() == null || profileEvent.getSkillIds().isEmpty()) {
            return true;
        }

        // If job has no skills, don't match (profile requires skills)
        if (jobEvent.getRequiredSkillIds() == null || jobEvent.getRequiredSkillIds().isEmpty()) {
            return false;
        }

        // Check for skill overlap
        Set<UUID> jobSkills = new HashSet<>(jobEvent.getRequiredSkillIds());
        Set<UUID> profileSkills = new HashSet<>(profileEvent.getSkillIds());

        for (UUID skillId : profileSkills) {
            if (jobSkills.contains(skillId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if fresher requirement matches.
     * 
     * Logic:
     * - If profile requires fresher jobs (is_fresher = true), job must be
     * fresher-friendly
     * - If profile doesn't care about fresher status, match any job
     */
    private boolean isFresherMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        // If profile doesn't specify fresher requirement, match any job
        if (profileEvent.getIsFresher() == null || !profileEvent.getIsFresher()) {
            return true;
        }

        // Profile requires fresher jobs
        if (jobEvent.getFresher() == null) {
            return false; // Uncertain, don't match
        }

        return jobEvent.getFresher();
    }

    /**
     * Check if job title matches.
     * 
     * Logic:
     * - If profile has no specific job title requirements, match any job
     * - If profile has job titles, job title must contain or partially match at
     * least one
     */
    private boolean isJobTitleMatch(JobPostingEvent jobEvent, UserSearchProfileUpdateEvent profileEvent) {
        // If profile has no specific job title requirements, match
        if (profileEvent.getJobTitles() == null || profileEvent.getJobTitles().isEmpty()) {
            return true;
        }

        // If job has no title, don't match
        if (jobEvent.getTitle() == null || jobEvent.getTitle().isEmpty()) {
            return false;
        }

        String jobTitle = jobEvent.getTitle().toLowerCase();

        // Check if job title contains or matches any of the profile's desired job
        // titles
        for (String desiredTitle : profileEvent.getJobTitles()) {
            if (desiredTitle != null && !desiredTitle.isEmpty()) {
                // Case-insensitive partial match
                if (jobTitle.contains(desiredTitle.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
