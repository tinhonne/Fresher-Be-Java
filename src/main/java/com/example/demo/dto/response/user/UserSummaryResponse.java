package com.example.demo.dto.response.user;

import com.example.demo.security.authorization.AppRole;
import java.util.Set;

public record UserSummaryResponse(
    Long id,
    String username,
    String name,
    boolean enabled,
    boolean mustChangePassword,
    Set<AppRole> roles) {}
