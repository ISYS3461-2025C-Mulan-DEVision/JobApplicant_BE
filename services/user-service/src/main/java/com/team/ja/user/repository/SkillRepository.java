package com.team.ja.user.repository;

import com.team.ja.user.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Skill entity.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    /**
     * Find all active skills.
     */
    List<Skill> findByIsActiveTrueOrderByUsageCountDesc();

    /**
     * Find skill by name (case insensitive).
     */
    Optional<Skill> findByNameIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Find skill by normalized name.
     */
    Optional<Skill> findByNormalizedNameAndIsActiveTrue(String normalizedName);

    /**
     * Search skills by name (partial match).
     */
    @Query("SELECT s FROM Skill s WHERE s.isActive = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.usageCount DESC")
    List<Skill> searchByName(@Param("query") String query);

    /**
     * Find popular skills (top N by usage count).
     */
    List<Skill> findTop20ByIsActiveTrueOrderByUsageCountDesc();

    /**
     * Check if skill name already exists.
     */
    boolean existsByNameIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Find skills by IDs.
     */
    List<Skill> findByIdInAndIsActiveTrue(List<UUID> ids);
}
