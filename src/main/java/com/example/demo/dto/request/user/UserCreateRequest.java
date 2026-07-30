package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
        @NotBlank @Size(max = 15) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 20) String name,
        Set<Long> roleIds
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