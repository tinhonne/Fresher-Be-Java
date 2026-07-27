package com.example.demo.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RoleCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,

        @NotEmpty(message = "Role phải có ít nhất 1 permission")
        Set<Long> permissionIds
) {}