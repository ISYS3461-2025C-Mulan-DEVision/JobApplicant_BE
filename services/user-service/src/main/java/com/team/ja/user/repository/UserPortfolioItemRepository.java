package com.team.ja.user.repository;

import com.team.ja.user.model.UserPortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPortfolioItemRepository extends JpaRepository<UserPortfolioItem, UUID> {
    List<UserPortfolioItem> findByUserId(UUID userId);
}
