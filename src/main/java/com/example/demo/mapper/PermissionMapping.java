package com.example.demo.mapper;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapping {
    Permission toEntity(PermissionRequest permissionRequest);
    PermissionResponse toResponse(Permission permission);
}
