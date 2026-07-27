package com.example.demo.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionRequest(
        @NotBlank @Size(max = 50) String code,       // vd: "TRANSACTION_APPROVE"
        @Size(max = 255) String description
) {
}
