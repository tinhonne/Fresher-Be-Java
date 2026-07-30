package com.example.demo.dto.request.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record RolePermissionRequest(
        @NotEmpty Set<@NotNull @Positive Long> permissionIds
) {
}
