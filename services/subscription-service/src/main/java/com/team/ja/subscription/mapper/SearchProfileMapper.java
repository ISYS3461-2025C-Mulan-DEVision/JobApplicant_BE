package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileResponse;
import com.team.ja.subscription.model.search_profile.SearchProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { SearchProfileSkillMapper.class, SearchProfileEmploymentMapper.class,
        SearchProfileJobTitleMapper.class })
public interface SearchProfileMapper {

    @Mapping(target = "employments", source = "employments")
    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "jobTitles", source = "jobTitles")
    SearchProfileResponse toResponse(SearchProfile profile);

}
