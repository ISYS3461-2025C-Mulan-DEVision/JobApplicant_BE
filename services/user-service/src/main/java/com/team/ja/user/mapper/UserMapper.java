package com.team.ja.user.mapper;

import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entity.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "country", ignore = true) // Set manually in service via CountryMapper
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
