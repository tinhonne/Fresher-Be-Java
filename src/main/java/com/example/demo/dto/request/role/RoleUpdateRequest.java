package com.example.demo.dto.request.role;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleUpdateRequest(
        @Size(max = 50) String name,
        @Size(max = 255) String description,
        Set<Long> permissionIds
) {
}
