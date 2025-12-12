package com.team.ja.user.service;

import com.team.ja.user.dto.response.SkillResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Skill operations.
 * 
 * Skills are reference data - users can select from existing skills
 * or request new ones to be added.
 */
public interface SkillService {

    /**
     * Get all active skills.
     * 
     * @return List of skills
     */
    List<SkillResponse> getAllSkills();

    /**
     * Get popular skills (most used).
     * 
     * @return List of popular skills
     */
    List<SkillResponse> getPopularSkills();

    /**
     * Search skills by name.
     * 
     * @param query Search query
     * @return List of matching skills
     */
    List<SkillResponse> searchSkills(String query);

    /**
     * Get skill by ID.
     * 
     * @param id Skill ID
     * @return Skill response
     */
    SkillResponse getSkillById(UUID id);

    /**
     * Add skills to a user.
     * 
     * @param userId User ID
     * @param skillIds Skill IDs to add
     * @return List of user's skills after adding
     */
    List<SkillResponse> addSkillsToUser(UUID userId, List<UUID> skillIds);

    /**
     * Remove a skill from a user.
     * 
     * @param userId User ID
     * @param skillId Skill ID to remove
     */
    void removeSkillFromUser(UUID userId, UUID skillId);

    /**
     * Get all skills for a user.
     * 
     * @param userId User ID
     * @return List of user's skills
     */
    List<SkillResponse> getUserSkills(UUID userId);

    /**
     * Create a new skill (admin or auto-create on user request).
     * 
     * @param name Skill name
     * @return Created skill
     */
    SkillResponse createSkill(String name);
}

