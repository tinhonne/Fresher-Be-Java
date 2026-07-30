package com.example.demo.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.example.demo.constant.ValidationConstants.*;

public record PermissionRequest(
        @NotBlank @Size(max = PERMISSION_CODE_MAX_LENGTH) String code,
        @Size(max = DESCRIPTION_MAX_LENGTH) String description
) {
}
