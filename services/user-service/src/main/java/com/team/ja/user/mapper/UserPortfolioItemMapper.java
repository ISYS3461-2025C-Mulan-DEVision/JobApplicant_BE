package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.UserPortfolioItemResponse;
import com.team.ja.user.model.UserPortfolioItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPortfolioItemMapper {
    UserPortfolioItemResponse toResponse(UserPortfolioItem item);
    List<UserPortfolioItemResponse> toResponseList(List<UserPortfolioItem> items);
}
