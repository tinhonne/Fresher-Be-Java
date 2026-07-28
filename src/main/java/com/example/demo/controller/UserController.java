package com.example.demo.controller;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserSummaryResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        return ApiResponse.success(userService.createUser(request));
    }


    @GetMapping
    public ApiResponse<List<UserSummaryResponse>> getListUser(){
        return ApiResponse.success(userService.getListUser());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id){
        return ApiResponse.success(userService.getUser(id));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(){
        return ApiResponse.success(userService.getMe());
    }
}
