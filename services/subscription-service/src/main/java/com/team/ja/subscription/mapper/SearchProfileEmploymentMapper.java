package com.team.ja.subscription.mapper;

import com.team.ja.subscription.dto.response.SearchProfileEmploymentResponse;
import com.team.ja.subscription.model.search_profile.SearchProfileEmployment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SearchProfileEmploymentMapper {
    SearchProfileEmploymentResponse toResponse(SearchProfileEmployment employment);
}
