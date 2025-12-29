package com.team.ja.user.api;

import com.team.ja.common.dto.ApiResponse;
import com.team.ja.common.exception.ForbiddenException;
import com.team.ja.user.dto.response.UserPortfolioItemResponse;
import com.team.ja.user.service.UserPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/portfolio")
@RequiredArgsConstructor
@Tag(name = "User Portfolio", description = "User portfolio management endpoints")
public class UserPortfolioController {

    private final UserPortfolioService userPortfolioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload portfolio item", description = "Upload a new image or video to a user's portfolio")
    public ApiResponse<UserPortfolioItemResponse> uploadPortfolioItem(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Authenticated User ID from JWT") @RequestHeader("X-User-Id") String authUserIdStr,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional description for the item") @RequestParam(required = false) String description) {
        
        authorize(userId, authUserIdStr);
        UserPortfolioItemResponse response = userPortfolioService.uploadItem(userId, file, description);
        return ApiResponse.success("Portfolio item uploaded successfully", response);
    }

    @GetMapping
    @Operation(summary = "Get portfolio items", description = "Get a list of all portfolio items for a user")
    public ApiResponse<List<UserPortfolioItemResponse>> getPortfolioItems(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        List<UserPortfolioItemResponse> items = userPortfolioService.getItemsForUser(userId);
        return ApiResponse.success("Portfolio items retrieved successfully", items);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete portfolio item", description = "Delete a specific item from a user's portfolio")
    public ApiResponse<Void> deletePortfolioItem(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "ID of the portfolio item to delete") @PathVariable UUID itemId,
            @Parameter(description = "Authenticated User ID from JWT") @RequestHeader("X-User-Id") String authUserIdStr) {
        
        authorize(userId, authUserIdStr);
        userPortfolioService.deleteItem(userId, itemId);
        return ApiResponse.success("Portfolio item deleted successfully", null);
    }

    private void authorize(UUID userIdFromPath, String authUserIdStr) {
        UUID authUserId = UUID.fromString(authUserIdStr);
        if (!userIdFromPath.equals(authUserId)) {
            throw new ForbiddenException("You are not authorized to access this resource.");
        }
    }
}
