package com.example.demo.dto.response.user;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String name,
        boolean enabled,
//        List<RoleSummary> roles,
        List<String> permissions   // flatten từ tất cả role, distinct
) {}