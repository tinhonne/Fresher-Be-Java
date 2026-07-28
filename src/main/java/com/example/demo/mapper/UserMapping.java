package com.example.demo.mapper;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapping {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserCreateRequest userCreateRequest);

    UserSummaryResponse toSumamary(User user);

    default UserResponse toResponse(User user){
        List<RoleSummaryResponse> roles=user.getRoles().stream()
                .map(role -> new RoleSummaryResponse(role.getId(), role.getName()))
                .toList();
        List<String> permissions=user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.isEnabled(),
                user.isMustChangePassword(),
                roles,
                permissions);
    }

}
