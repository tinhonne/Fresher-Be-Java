package com.example.demo.controller;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.createUser(request)));
    }
    @GetMapping
    public ApiResponse<List<UserResponse>> findUser(){
        return ApiResponse.success(userService.findUser());
    }

    @GetMapping("/{id}")
    public  ApiResponse<UserResponse> findUserId(@PathVariable Long id){
        return ApiResponse.success(userService.findUserbyId(id));
    }

    @GetMapping("/myinfo")
    public ApiResponse<UserResponse> getMyInfo(){
        return  ApiResponse.success(userService.getMyInfo());
    }
}
