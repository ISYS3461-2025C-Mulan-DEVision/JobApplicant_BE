package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SearchProfileMapper {

    @Mapping(target = "employments", source = "employments")
    @Mapping(target = "skills", source = "skills")
    SearchProfileResponse toResponse(SearchProfile profile);

}
