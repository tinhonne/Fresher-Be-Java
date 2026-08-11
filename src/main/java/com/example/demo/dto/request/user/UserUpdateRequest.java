package com.example.demo.dto.request.user;

import static com.example.demo.constant.ValidationConstants.NON_BLANK_PATTERN;
import static com.example.demo.constant.ValidationConstants.USER_NAME_MAX_LENGTH;

import com.example.demo.security.authorization.AppRole;
import com.example.demo.validation.annotation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

@AtLeastOneFieldNotNull(fields = {"name", "roles"})
public record UserUpdateRequest(
    @Pattern(regexp = NON_BLANK_PATTERN) @Size(max = USER_NAME_MAX_LENGTH) String name,
    Set<@NotNull AppRole> roles) {}
