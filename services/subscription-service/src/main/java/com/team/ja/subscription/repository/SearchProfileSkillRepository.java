package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team.ja.subscription.model.search_profile.SearchProfileSkill;

/**
 * Repository for SearchProfileSkill entity.
 */
@Repository
public interface SearchProfileSkillRepository extends JpaRepository<SearchProfileSkill, UUID> {

    /**
     * Find all skills associated with a given search profile ID.
     */
    List<SearchProfileSkill> findBySearchProfileId(UUID searchProfileId);

    /**
     * Find all active skills associated with a given search profile ID.
     */
    List<SearchProfileSkill> findBySearchProfileIdAndIsActiveTrue(UUID searchProfileId);

    /**
     * Find an active skill by id and user id.
     */
    java.util.Optional<SearchProfileSkill> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);

    /**
     * Find a skill by search profile id and skill id (active or not).
     */
    java.util.Optional<SearchProfileSkill> findBySearchProfileIdAndSkillId(UUID searchProfileId, UUID skillId);

    /**
     * Delete all skills associated with a given search profile ID.
     */
    void deleteBySearchProfileId(UUID searchProfileId);

}
