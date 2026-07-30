package com.example.demo.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

import static com.example.demo.constant.ValidationConstants.*;

public record RoleCreateRequest(
        @NotBlank @Size(max = ROLE_NAME_MAX_LENGTH) String name,
        @Size(max = DESCRIPTION_MAX_LENGTH) String description,
        Set<@NotNull @Positive Long> permissionIds
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