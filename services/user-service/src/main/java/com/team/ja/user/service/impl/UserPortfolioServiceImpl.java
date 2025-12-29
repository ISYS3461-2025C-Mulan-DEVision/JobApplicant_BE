package com.team.ja.user.service.impl;

import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.FileSizeExceededException;
import com.team.ja.common.exception.StorageException;
import com.team.ja.common.exception.UnsupportedFileTypeException;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.S3FileService;
import com.team.ja.user.dto.response.UserPortfolioItemResponse;
import com.team.ja.user.mapper.UserPortfolioItemMapper;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserPortfolioItem;
import com.team.ja.user.repository.UserPortfolioItemRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.UserPortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserPortfolioServiceImpl implements UserPortfolioService {

    private final UserRepository userRepository;
    private final UserPortfolioItemRepository portfolioItemRepository;
    private final S3FileService s3FileService;
    private final UserPortfolioItemMapper portfolioItemMapper;

    private static final List<String> SUPPORTED_MEDIA_TYPES = List.of("image/jpeg", "image/png", "image/gif", "video/mp4");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Override
    public UserPortfolioItemResponse uploadItem(UUID userId, MultipartFile file, String description) {
        log.info("Uploading portfolio item for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        validateFile(file);

        try {
            String fileUrl = s3FileService.uploadFile(file, "portfolio/" + userId);

            UserPortfolioItem item = UserPortfolioItem.builder()
                    .user(user)
                    .fileUrl(fileUrl)
                    .description(description)
                    .mediaType(file.getContentType())
                    .build();

            UserPortfolioItem savedItem = portfolioItemRepository.save(item);
            log.info("Successfully uploaded portfolio item {} for user {}", savedItem.getId(), userId);
            return portfolioItemMapper.toResponse(savedItem);
        } catch (IOException e) {
            log.error("Failed to upload portfolio item for user {}: {}", userId, e.getMessage());
            throw new StorageException("Could not store file. Please try again.", e);
        }
    }

    @Override
    public void deleteItem(UUID userId, UUID itemId) {
        log.info("Deleting portfolio item {} for user {}", itemId, userId);

        UserPortfolioItem item = portfolioItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Portfolio item", "id", itemId.toString()));

        if (!item.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this portfolio item.");
        }

        s3FileService.deleteFile(item.getFileUrl());
        portfolioItemRepository.delete(item);
        log.info("Successfully deleted portfolio item {}", itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPortfolioItemResponse> getItemsForUser(UUID userId) {
        log.info("Fetching portfolio items for user {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
        List<UserPortfolioItem> items = portfolioItemRepository.findByUserId(userId);
        return portfolioItemMapper.toResponseList(items);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty.");
        }
        if (!SUPPORTED_MEDIA_TYPES.contains(file.getContentType())) {
            throw new UnsupportedFileTypeException("Unsupported media type. Supported types are: " + SUPPORTED_MEDIA_TYPES);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("File size exceeds the limit of 10MB.");
        }
    }
}