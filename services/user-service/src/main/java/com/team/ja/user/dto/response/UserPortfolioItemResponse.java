package com.team.ja.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPortfolioItemResponse {
    private UUID id;
    private String fileUrl;
    private String description;
    private String mediaType;
}
