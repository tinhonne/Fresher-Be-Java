package com.example.demo.dto.response.role;


import com.example.demo.dto.response.permission.PermissionResponse;

import java.util.List;

public record RoleResponse(
        Long id,
        String name,
        String description,
        List<PermissionResponse> permissions
) {}