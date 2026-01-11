package com.team.ja.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.user.model.UserSearchProfile;

@Repository
public interface UserSearchProfileRepository extends JpaRepository<UserSearchProfile, UUID> {

    Optional<UserSearchProfile> findByUserId(UUID userId);

    Optional<UserSearchProfile> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserSearchProfile> findByIdAndIsActiveTrue(UUID id);

    List<UserSearchProfile> findByIsActiveTrue();

}
