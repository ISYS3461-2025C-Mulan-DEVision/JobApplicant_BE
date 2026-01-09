package com.team.ja.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.ja.user.model.UserSearchProfileSkill;

public interface UserSearchProfileSkillRepository extends JpaRepository<UserSearchProfileSkill, UUID> {

    List<UserSearchProfileSkill> findByUserSearchProfileIdAndIsActiveTrue(UUID userSearchProfileId);

    List<UserSearchProfileSkill> findByUserSearchProfileId(UUID userSearchProfileId);

    boolean existsByUserSearchProfileId(UUID userSearchProfileId);

    Optional<UserSearchProfileSkill> findByUserSearchProfileIdAndSkillIdAndIsActiveTrue(UUID userSearchProfileId,
            UUID skillId);

}
