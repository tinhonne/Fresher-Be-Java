package com.example.demo.dto.response.user;

import com.example.demo.dto.response.role.RoleSummaryResponse;

import java.util.List;

public record UserSummaryResponse(
        Long id,
        String username,
        String name,
        boolean enabled,
        boolean mustChangePassword,
        List<RoleSummaryResponse> roles
) {}
