package com.team.ja.user.repository;

import com.team.ja.user.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Country entity.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    List<Country> findByIsActiveTrueOrderByNameAsc();

    Optional<Country> findByAbbreviation(String abbreviation);
}

