package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.UserWorkExperienceResponse;
import com.team.ja.user.model.UserWorkExperience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for UserWorkExperience entity.
 */
@Mapper(componentModel = "spring")
public interface UserWorkExperienceMapper {

    @Mapping(target = "employmentTypeDisplayName", expression = "java(experience.getEmploymentType() != null ? experience.getEmploymentType().getDisplayName() : null)")
    @Mapping(target = "country", ignore = true) // Set manually in service via CountryMapper
    UserWorkExperienceResponse toResponse(UserWorkExperience experience);

    List<UserWorkExperienceResponse> toResponseList(List<UserWorkExperience> experienceList);
}
