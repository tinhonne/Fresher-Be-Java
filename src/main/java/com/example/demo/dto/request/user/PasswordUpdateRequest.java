package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 100) String newPassword
) {
}
