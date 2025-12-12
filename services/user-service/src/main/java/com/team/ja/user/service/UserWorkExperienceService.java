package com.team.ja.user.service;

import com.team.ja.user.dto.request.CreateUserWorkExperienceRequest;
import com.team.ja.user.dto.request.UpdateUserWorkExperienceRequest;
import com.team.ja.user.dto.response.UserWorkExperienceResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User Work Experience operations.
 * 
 * Auth Integration Notes:
 * - All operations require authenticated user
 * - userId will come from JWT token
 * - User can only manage their own work experience records
 */
public interface UserWorkExperienceService {

    /**
     * Create a new work experience entry for a user.
     * 
     * @param userId User ID (from JWT in production)
     * @param request Work experience data
     * @return Created work experience response
     */
    UserWorkExperienceResponse createWorkExperience(UUID userId, CreateUserWorkExperienceRequest request);

    /**
     * Update a work experience entry.
     * 
     * @param userId User ID (from JWT in production)
     * @param workExpId Work experience record ID
     * @param request Update data
     * @return Updated work experience response
     */
    UserWorkExperienceResponse updateWorkExperience(UUID userId, UUID workExpId, UpdateUserWorkExperienceRequest request);

    /**
     * Get all work experience entries for a user.
     * 
     * @param userId User ID
     * @return List of work experience records
     */
    List<UserWorkExperienceResponse> getWorkExperienceByUserId(UUID userId);

    /**
     * Get a specific work experience entry.
     * 
     * @param userId User ID
     * @param workExpId Work experience record ID
     * @return Work experience response
     */
    UserWorkExperienceResponse getWorkExperienceById(UUID userId, UUID workExpId);

    /**
     * Delete (soft delete) a work experience entry.
     * 
     * @param userId User ID
     * @param workExpId Work experience record ID
     */
    void deleteWorkExperience(UUID userId, UUID workExpId);
}

