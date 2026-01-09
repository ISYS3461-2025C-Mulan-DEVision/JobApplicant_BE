package com.team.ja.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.ja.user.model.UserSearchProfileJobTitle;

@Repository
public interface UserSearchProfileJobTitleRepository extends JpaRepository<UserSearchProfileJobTitle, UUID> {

    List<UserSearchProfileJobTitle> findByUserSearchProfileIdAndIsActiveTrue(UUID userSearchProfileId);

    List<UserSearchProfileJobTitle> findByUserSearchProfileId(UUID userSearchProfileId);

    UserSearchProfileJobTitle findByIdAndUserSearchProfileId(UUID id, UUID userSearchProfileId);
}