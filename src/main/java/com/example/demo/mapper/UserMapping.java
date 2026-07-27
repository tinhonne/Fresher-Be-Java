package com.example.demo.mapper;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapping {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserCreateRequest userCreateRequest);

    UserSummaryResponse toResponse(User user);

}
