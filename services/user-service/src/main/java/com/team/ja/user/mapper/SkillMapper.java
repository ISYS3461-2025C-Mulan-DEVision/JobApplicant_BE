package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.model.Skill;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Skill entity.
 */
@Mapper(componentModel = "spring")
public interface SkillMapper {

    SkillResponse toResponse(Skill skill);

    List<SkillResponse> toResponseList(List<Skill> skills);
}
