package com.example.demo.dto.request.role;

import com.example.demo.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

import static com.example.demo.constant.ValidationConstants.DESCRIPTION_MAX_LENGTH;
import static com.example.demo.constant.ValidationConstants.NON_BLANK_PATTERN;
import static com.example.demo.constant.ValidationConstants.ROLE_NAME_MAX_LENGTH;

@AtLeastOneFieldNotNull(fields = {"name", "description", "permissionIds"})
public record RoleUpdateRequest(
        @Pattern(regexp = NON_BLANK_PATTERN) @Size(max = ROLE_NAME_MAX_LENGTH) String name,
        @Size(max = DESCRIPTION_MAX_LENGTH) String description,
        Set<@NotNull @Positive Long> permissionIds
) {
}
