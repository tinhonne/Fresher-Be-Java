package com.example.demo.mapper;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapping {

    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleCreateRequest roleCreateRequest);

    RoleResponse toResponse(Role role);
    RoleSummaryResponse toResponseOptions(Role role);
}
