package com.team.ja.user.service;

import com.team.ja.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User operations.
 */
public interface UserService {

    /**
     * Get all active users.
     */
    List<UserResponse> getAllUsers();

    /**
     * Get user by ID.
     */
    UserResponse getUserById(UUID id);

    /**
     * Get user by email.
     */
    UserResponse getUserByEmail(String email);
}

