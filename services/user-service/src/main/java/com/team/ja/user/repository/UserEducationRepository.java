package com.team.ja.user.repository;

import com.team.ja.user.model.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserEducation entity.
 */
@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, UUID> {

    /**
     * Find all active education records for a user.
     */
    List<UserEducation> findByUserIdAndIsActiveTrueOrderByStartAtDesc(UUID userId);

    /**
     * Find a specific education record by ID and user ID.
     */
    Optional<UserEducation> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);

    /**
     * Check if education record exists for user.
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);

    /**
     * Count active education records for a user.
     */
    long countByUserIdAndIsActiveTrue(UUID userId);
}

