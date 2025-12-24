package com.team.ja.user.service;

import com.team.ja.user.dto.request.CreateUserRequest;
import com.team.ja.user.dto.request.UpdateUserRequest;
import com.team.ja.user.dto.response.UserProfileResponse;
import com.team.ja.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User operations.
 * 
 * Auth Integration Notes:
 * - createUser: Called by auth-service after successful registration
 * - updateUser: userId will come from JWT token (authenticated user can only update their own profile)
 * - deactivateUser: Admin only, or user deactivating their own account
 * - getUserById: Public for basic info, full profile requires authentication
 */
public interface UserService {

    /**
     * Create a new user profile.
     * Called internally by auth-service after registration.
     * 
     * @param request User creation data
     * @return Created user response
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Update user profile.
     * Auth: User can only update their own profile.
     * 
     * @param userId User ID (from JWT token in production)
     * @param request Update data
     * @return Updated user response
     */
    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    /**
     * Upload and update user avatar.
     * @param userId User ID
     * @param file Image file for the avatar
     * @return Updated user response
     */
    UserResponse uploadAvatar(UUID userId, org.springframework.web.multipart.MultipartFile file);

    /**
     * Get user by ID.
     * 
     * @param id User ID
     * @return User response
     */
    UserResponse getUserById(UUID id);

    /**
     * Get user by email.
     * Used for looking up user during authentication.
     * 
     * @param email User email
     * @return User response
     */
    UserResponse getUserByEmail(String email);

    /**
     * Get complete user profile with education, work experience, and skills.
     * 
     * @param userId User ID
     * @return Complete profile response
     */
    UserProfileResponse getUserProfile(UUID userId);

    /**
     * Get all active users.
     * Auth: Admin only in production.
     * 
     * @return List of users
     */
    List<UserResponse> getAllUsers();

    /**
     * Deactivate (soft delete) a user.
     * Auth: Admin or user themselves.
     * 
     * @param userId User ID
     */
    void deactivateUser(UUID userId);

    /**
     * Reactivate a deactivated user.
     * Auth: Admin only.
     * 
     * @param userId User ID
     * @return Reactivated user response
     */
    UserResponse reactivateUser(UUID userId);

    /**
     * Check if email already exists.
     * 
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);
}
