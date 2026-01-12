package com.team.ja.auth.repository;

import com.team.ja.auth.model.AuthCredential;
import com.team.ja.auth.model.TokenType;
import com.team.ja.auth.model.VerificationToken;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for VerificationToken entity.
 */
@Repository
public interface VerificationTokenRepository
    extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    void deleteByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime cutoff);
    
    void deleteByCredential(AuthCredential credential);

    Optional<VerificationToken> findFirstByCredentialAndTokenTypeOrderByCreatedAtDesc(AuthCredential credential, TokenType tokenType);

    void deleteByCredentialAndTokenType(AuthCredential credential, TokenType tokenType);
}
