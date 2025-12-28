package com.team.ja.user.repository;

import com.team.ja.user.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSkill entity.
 */
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    /**
     * Find all active skills for a user.
     */
    List<UserSkill> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find all skill relations for a user, including inactive ones.
     */
    List<UserSkill> findByUserId(UUID userId);

    /**
     * Find a specific user-skill mapping.
     */
    Optional<UserSkill> findByUserIdAndSkillIdAndIsActiveTrue(UUID userId, UUID skillId);

    /**
     * Find a specific user-skill mapping (including inactive).
     */
    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);

    /**
     * Check if user already has this skill.
     */
    boolean existsByUserIdAndSkillIdAndIsActiveTrue(UUID userId, UUID skillId);

    /**
     * Count active skills for a user.
     */
    long countByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Delete all skills for a user (for bulk replacement).
     */
    void deleteByUserId(UUID userId);
}
