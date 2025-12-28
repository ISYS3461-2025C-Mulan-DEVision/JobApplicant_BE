package com.team.ja.user.service;

import com.team.ja.user.dto.response.UserPortfolioItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserPortfolioService {

    UserPortfolioItemResponse uploadItem(UUID userId, MultipartFile file, String description);

    void deleteItem(UUID userId, UUID itemId);

    List<UserPortfolioItemResponse> getItemsForUser(UUID userId);
}
