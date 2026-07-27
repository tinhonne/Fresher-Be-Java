package com.example.demo.controller;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserSummaryResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        return ApiResponse.success(userService.createUser(request));
    }
}
