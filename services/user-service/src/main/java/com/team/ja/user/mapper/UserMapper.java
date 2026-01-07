package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserSkill;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MapStruct mapper for User entity.
 */
@Mapper(componentModel = "spring", uses = {UserEducationMapper.class, UserWorkExperienceMapper.class, UserPortfolioItemMapper.class})
public abstract class UserMapper {

    @Autowired
    protected SkillMapper skillMapper;

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "country", ignore = true) // Set manually in service via CountryMapper
    @Mapping(target = "skills", expression = "java(mapSkills(user.getUserSkills()))")
    @Mapping(target = "education", source = "education")
    @Mapping(target = "workExperience", source = "workExperience")
    @Mapping(target = "portfolioItems", source = "portfolioItems")
    public abstract UserResponse toResponse(User user);

    public abstract List<UserResponse> toResponseList(List<User> users);

    protected List<SkillResponse> mapSkills(Set<UserSkill> userSkills) {
        if (userSkills == null) {
            return java.util.Collections.emptyList();
        }
        return userSkills.stream()
                .map(UserSkill::getSkill)
                .filter(Objects::nonNull)
                .map(skillMapper::toResponse)
                .toList();
    }
}