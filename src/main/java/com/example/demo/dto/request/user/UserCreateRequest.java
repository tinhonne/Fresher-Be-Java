package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 50) String name,

        Set<Long> roleIds   // null/rỗng -> fallback role mặc định (theo config)
){
    public UserCreateRequest{
        if(roleIds==null){
            roleIds=Set.of();
        }
    }
}