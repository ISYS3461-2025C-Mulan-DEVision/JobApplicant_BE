package com.team.ja.user.service;

import com.team.ja.user.dto.request.CreateUserEducationRequest;
import com.team.ja.user.dto.request.UpdateUserEducationRequest;
import com.team.ja.user.dto.response.UserEducationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User Education operations.
 * 
 * Auth Integration Notes:
 * - All operations require authenticated user
 * - userId will come from JWT token
 * - User can only manage their own education records
 */
public interface UserEducationService {

    /**
     * Create a new education entry for a user.
     * 
     * @param userId User ID (from JWT in production)
     * @param request Education data
     * @return Created education response
     */
    UserEducationResponse createEducation(UUID userId, CreateUserEducationRequest request);

    /**
     * Update an education entry.
     * 
     * @param userId User ID (from JWT in production)
     * @param educationId Education record ID
     * @param request Update data
     * @return Updated education response
     */
    UserEducationResponse updateEducation(UUID userId, UUID educationId, UpdateUserEducationRequest request);

    /**
     * Get all education entries for a user.
     * 
     * @param userId User ID
     * @return List of education records
     */
    List<UserEducationResponse> getEducationByUserId(UUID userId);

    /**
     * Get a specific education entry.
     * 
     * @param userId User ID
     * @param educationId Education record ID
     * @return Education response
     */
    UserEducationResponse getEducationById(UUID userId, UUID educationId);

    /**
     * Delete (soft delete) an education entry.
     * 
     * @param userId User ID
     * @param educationId Education record ID
     */
    void deleteEducation(UUID userId, UUID educationId);
}

