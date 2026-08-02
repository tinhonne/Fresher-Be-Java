package com.example.demo.dto.request.user;

import com.example.demo.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

import static com.example.demo.constant.ValidationConstants.NON_BLANK_PATTERN;
import static com.example.demo.constant.ValidationConstants.USER_NAME_MAX_LENGTH;

@AtLeastOneFieldNotNull(fields = {"name", "roleIds"})
public record UserUpdateRequest(
        @Pattern(regexp = NON_BLANK_PATTERN) @Size(max = USER_NAME_MAX_LENGTH) String name,
        Set<@NotNull @Positive Long> roleIds
) {
}
