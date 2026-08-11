package com.example.demo.mapper;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapping {
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "password", ignore = true)
  User toEntity(UserCreateRequest userCreateRequest);

  UserSummaryResponse toSumamary(User user);

  default UserResponse toResponse(User user) {
    List<String> permissions =
        user.getRoles().stream()
            .flatMap(
                role ->
                    java.util.Arrays.stream(
                            com.example.demo.security.authorization.AppPermission.values())
                        .filter(role::hasPermission))
            .map(Enum::name)
            .distinct()
            .sorted()
            .toList();
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getName(),
        user.isEnabled(),
        user.isMustChangePassword(),
        Set.copyOf(user.getRoles()),
        permissions);
  }
}
