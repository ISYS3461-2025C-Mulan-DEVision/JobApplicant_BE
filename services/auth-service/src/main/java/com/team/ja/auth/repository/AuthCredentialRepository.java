package com.team.ja.auth.repository;

import com.team.ja.auth.model.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AuthCredential entity.
 */
@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredential, UUID> {

    Optional<AuthCredential> findByEmailAndIsActiveTrue(String email);

    Optional<AuthCredential> findByEmail(String email);

    Optional<AuthCredential> findByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsActiveTrue(String email);
}

