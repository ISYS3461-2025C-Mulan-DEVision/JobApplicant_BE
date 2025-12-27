package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileSkillResponse;
import com.team.ja.subscription.model.search_profile.SearchProfileSkill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SearchProfileSkillMapper {
    SearchProfileSkillResponse toResponse(SearchProfileSkill skill);
}
