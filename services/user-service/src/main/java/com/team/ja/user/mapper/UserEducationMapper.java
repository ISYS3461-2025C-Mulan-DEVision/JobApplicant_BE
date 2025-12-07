package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.model.UserEducation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for UserEducation entity.
 */
@Mapper(componentModel = "spring")
public interface UserEducationMapper {

    @Mapping(target = "educationLevelDisplayName", expression = "java(education.getEducationLevel() != null ? education.getEducationLevel().getDisplayName() : null)")
    UserEducationResponse toResponse(UserEducation education);

    List<UserEducationResponse> toResponseList(List<UserEducation> educationList);
}

