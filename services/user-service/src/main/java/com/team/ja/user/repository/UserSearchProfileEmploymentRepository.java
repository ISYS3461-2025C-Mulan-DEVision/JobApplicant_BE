package com.team.ja.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.ja.user.model.UserSearchProfileEmploymentStatus;

public interface UserSearchProfileEmploymentRepository extends JpaRepository<UserSearchProfileEmploymentStatus, UUID> {

    List<UserSearchProfileEmploymentStatus> findByUserSearchProfileIdAndIsActiveTrue(UUID userSearchProfileId);

    List<UserSearchProfileEmploymentStatus> findByUserSearchProfileId(UUID userSearchProfileId);

}
