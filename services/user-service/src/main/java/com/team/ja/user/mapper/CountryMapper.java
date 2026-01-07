package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.CountryResponse;
import com.team.ja.user.model.Country;

import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Country entity.
 */
@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);

    List<CountryResponse> toResponseList(List<Country> countries);
}
