package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SearchProfileMapper {

    SearchProfileMapper INSTANCE = Mappers.getMapper(SearchProfileMapper.class);

    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "employments", source = "employments")
    SearchProfileResponse toResponse(SearchProfile profile);

}
