package com.team.ja.user.repository;

import com.team.ja.user.model.UserWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserWorkExperience entity.
 */
@Repository
public interface UserWorkExperienceRepository extends JpaRepository<UserWorkExperience, UUID> {

    /**
     * Find all active work experiences for a user.
     */
    List<UserWorkExperience> findByUserIdAndIsActiveTrueOrderByStartAtDesc(UUID userId);

    /**
     * Find a specific work experience by ID and user ID.
     */
    Optional<UserWorkExperience> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);

    /**
     * Check if work experience exists for user.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);

    /**
     * Count active work experiences for a user.
     */
    long countByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find current job (isCurrent = true) for a user.
     */
    Optional<UserWorkExperience> findByUserIdAndIsCurrentTrueAndIsActiveTrue(UUID userId);
}

