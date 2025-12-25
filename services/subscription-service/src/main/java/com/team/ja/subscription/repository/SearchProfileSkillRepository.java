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
     * Delete all skills associated with a given search profile ID.
     */
    void deleteBySearchProfileId(UUID searchProfileId);

}
