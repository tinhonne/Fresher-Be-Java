package com.example.demo.dto.request.role;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RolePermissionRequest(
        @NotEmpty Set<Long> permissionIds
) {
}
