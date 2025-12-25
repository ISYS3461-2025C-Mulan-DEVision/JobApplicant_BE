package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.search_profile.SearchProfileEmployment;

@Repository
public interface SearchProfileEmploymentRepository extends JpaRepository<SearchProfileEmployment, UUID> {

    /**
     * Find all employments associated with a given search profile ID.
     */
    List<SearchProfileEmployment> findBySearchProfileId(UUID searchProfileId);

    /**
     * Delete all employments associated with a given search profile ID.
     */
    void deleteBySearchProfileId(UUID searchProfileId);

    /**
     * Find all employments associated with a given user ID.
     */
    List<SearchProfileEmployment> findByUserId(UUID userId);

    /**
     * Delete all employments associated with a given user ID.
     */
    void deleteByUserId(UUID userId);

}
