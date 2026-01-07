package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.UserSkillResponse;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for UserSkill entity.
 * Note: This mapper requires the Skill entity to be fetched separately.
 */
@Mapper(componentModel = "spring")
public interface UserSkillMapper {

    /**
     * Map UserSkill with its associated Skill to response.
     * The skill must be fetched and provided separately.
     */
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "createdAt", source = "userSkill.createdAt")
    UserSkillResponse toResponse(UserSkill userSkill, Skill skill);
}
