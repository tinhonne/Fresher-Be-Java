package com.example.demo.service;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    List<UserResponse> findUser();
    UserResponse findUserbyId(long id);
    UserResponse getMyInfo();
}
