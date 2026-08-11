package com.example.demo.dto.response.user;

import com.example.demo.security.authorization.AppRole;
import java.util.List;
import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String name,
    boolean enabled,
    boolean mustChangePassword,
    Set<AppRole> roles,
    List<String> permissions // flatten từ tất cả role, distinct
    ) {}
