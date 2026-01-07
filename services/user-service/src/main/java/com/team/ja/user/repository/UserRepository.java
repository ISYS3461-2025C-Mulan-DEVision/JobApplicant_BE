package com.team.ja.user.repository;

import com.team.ja.user.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository
        extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE fts_document @@ websearch_to_tsquery('english', :query)", nativeQuery = true)
    List<User> findByFts(@Param("query") String query);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userSkills " +
            "LEFT JOIN FETCH u.education " +
            "LEFT JOIN FETCH u.workExperience " +
            "LEFT JOIN FETCH u.portfolioItems " +
            "WHERE u.id = :userId")
    Optional<User> findFullUserById(@Param("userId") UUID userId);
}
