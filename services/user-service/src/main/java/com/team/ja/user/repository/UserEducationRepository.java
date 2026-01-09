package com.team.ja.user.repository;

import com.team.ja.user.model.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    List<UserEducation> findByUserId(UUID userId);

    /**
     * Find all active education records for a user.
     *
     */
    @Query(value = "SELECT * FROM user_education u WHERE u.user_id = :userId AND u.is_active = true " +
            "ORDER BY array_position(ARRAY['HIGH_SCHOOL','VOCATIONAL','ASSOCIATE','BACHELOR','MASTER','DOCTORATE','PROFESSIONAL'], u.education_level) DESC", nativeQuery = true)
    List<UserEducation> findByUserIdOrderByEducationLevelRankDesc(UUID userId);

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
