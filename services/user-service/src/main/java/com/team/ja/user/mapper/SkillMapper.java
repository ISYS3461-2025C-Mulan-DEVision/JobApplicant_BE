package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.dto.response.UserSearchProfileSkillResponse;
import com.team.ja.user.model.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Skill entity.
 */
@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(target = "id", expression = "java(skill.getId() != null ? skill.getId().toString() : null)")
    SkillResponse toResponse(Skill skill);

    List<SkillResponse> toResponseList(List<Skill> skills);

    List<UserSearchProfileSkillResponse> toUserSearchProfileSkillResponseList(List<Skill> skills);
}
