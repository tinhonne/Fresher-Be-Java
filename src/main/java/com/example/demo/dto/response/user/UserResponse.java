package com.example.demo.dto.response.user;

import com.example.demo.dto.response.role.RoleSummaryResponse;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String name,
        boolean enabled,
        boolean mustChangePassword,
        List<RoleSummaryResponse> roles,
        List<String> permissions   // flatten từ tất cả role, distinct
) {}