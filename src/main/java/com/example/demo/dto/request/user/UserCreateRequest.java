package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

import static com.example.demo.constant.ValidationConstants.*;

public record UserCreateRequest(
        @NotBlank @Size(max = USERNAME_MAX_LENGTH) String username,
        @NotBlank @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH) String password,
        @NotBlank @Size(max = USER_NAME_MAX_LENGTH) String name,
        Set<@NotNull @Positive Long> roleIds
){
    /**
     * Creates a user request and normalizes missing role identifiers to an empty set.
     *
     * @param username the username
     * @param password the password
     * @param name the user's name
     * @param roleIds the role identifiers
     */
    public UserCreateRequest{
        if(roleIds==null){
            roleIds=Set.of();
        }
    }
}