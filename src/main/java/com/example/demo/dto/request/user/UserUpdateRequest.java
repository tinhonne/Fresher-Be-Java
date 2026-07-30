package com.example.demo.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
        @Pattern(regexp = ".*\\S.*") @Size(max = 20) String name,
        Set<Long> roleIds
) {
}
