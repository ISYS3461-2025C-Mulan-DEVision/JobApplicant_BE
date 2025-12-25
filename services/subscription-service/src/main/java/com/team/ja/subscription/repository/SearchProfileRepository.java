package com.team.ja.subscription.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.subscription.model.search_profile.SearchProfile;

/**
 * Repository for SearchProfile entity.
 */
@Repository
public interface SearchProfileRepository extends JpaRepository<SearchProfile, UUID> {

    /**
     * Find search profile by user ID.
     */
    SearchProfile findByUserId(UUID userId);

    /**
     * Check if a search profile exists for a given user ID.
     */
    boolean existsByUserId(UUID userId);

}
