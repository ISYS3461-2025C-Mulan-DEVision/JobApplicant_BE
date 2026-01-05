package com.team.ja.user.dto.request;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPortfolioItemDto {

    private UUID id;
    private String fileUrl;
    private String description;
    private String mediaType;

}
