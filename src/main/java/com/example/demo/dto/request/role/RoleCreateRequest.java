package com.example.demo.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RoleCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,
        Set<Long> permissionIds
) {
    /**
     * Creates a role request and normalizes missing permission identifiers to an empty set.
     *
     * @param name the role name
     * @param description the optional role description
     * @param permissionIds the permission identifiers
     */
    public RoleCreateRequest {
        if (permissionIds == null) {
            permissionIds = Set.of();
        }
    }
}