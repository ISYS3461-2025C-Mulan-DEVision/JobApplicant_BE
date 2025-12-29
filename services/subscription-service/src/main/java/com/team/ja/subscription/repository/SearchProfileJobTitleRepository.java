package com.team.ja.subscription.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.ja.subscription.model.search_profile.SearchProfileJobTitle;

public interface SearchProfileJobTitleRepository extends JpaRepository<SearchProfileJobTitle, UUID> {

    List<SearchProfileJobTitle> findBySearchProfileId(UUID searchProfileId);

    List<SearchProfileJobTitle> findByUserId(UUID userId);

}
