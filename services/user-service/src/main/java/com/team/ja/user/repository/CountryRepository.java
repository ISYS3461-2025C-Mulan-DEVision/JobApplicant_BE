package com.team.ja.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.user.model.Country;

/**
 * Repository for Country entity.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {
        List<Country> findByIsActiveTrueOrderByNameAsc();

        Optional<Country> findByAbbreviationIgnoreCase(String abbreviation);

        Optional<Country> findByIdAndIsActiveTrue(java.util.UUID id);

        Optional<Country> findByAbbreviationIgnoreCaseAndIsActiveTrue(
                        String abbreviation);

        List<Country> findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndAbbreviationContainingIgnoreCase(
                        String name,
                        String abbreviation);
}
