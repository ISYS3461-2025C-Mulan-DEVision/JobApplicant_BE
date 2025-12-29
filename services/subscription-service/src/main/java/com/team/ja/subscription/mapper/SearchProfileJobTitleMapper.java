package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileJobTitleResponse;
import com.team.ja.subscription.model.search_profile.SearchProfileJobTitle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SearchProfileJobTitleMapper {

    SearchProfileJobTitleResponse toResponse(SearchProfileJobTitle jobTitle);

}
