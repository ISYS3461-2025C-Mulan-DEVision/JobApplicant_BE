package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;
import com.team.ja.subscription.model.search_profile.SearchProfileSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SearchProfileSkillMapper {

    @Mapping(target = "skillId", source = "skill.id")
    SearchProfileSkillResponse toResponse(SearchProfileSkill skill);
}
