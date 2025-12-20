package com.team.ja.auth.repository;

import com.team.ja.auth.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VerificationToken entity.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByToken(String token);
}
