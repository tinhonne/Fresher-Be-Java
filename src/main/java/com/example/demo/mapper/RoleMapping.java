package com.example.demo.mapper;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapping {

    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleCreateRequest roleCreateRequest);

    /**
     * Maps a role to a response whose permissions are ordered by identifier ascending.
     * The role and its permission collection must be non-null.
     *
     * @param role the role to map
     * @return the role response with deterministically sorted permissions
     */
    default RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(permission -> new PermissionResponse(
                        permission.getId(), permission.getCode(), permission.getDescription()))
                .sorted(Comparator.comparing(PermissionResponse::id))
                .toList();
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), permissions);
    }

    RoleSummaryResponse toResponseOptions(Role role);
}
