package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileEmployment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SearchProfileEmploymentMapper {
    SearchProfileEmployment toResponse(SearchProfileEmployment employment);
}
