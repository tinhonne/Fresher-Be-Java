package com.example.demo.dto.request.user;

import static com.example.demo.constant.ValidationConstants.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
    @NotBlank String oldPassword,
    @NotBlank @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH) String newPassword) {}
