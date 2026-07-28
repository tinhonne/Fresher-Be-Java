package com.example.demo.service;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;

import java.util.List;

public interface UserService {
    UserSummaryResponse createUser(UserCreateRequest userCreateRequest);
    List<UserSummaryResponse> getListUser();
    UserResponse getMyInfo();
}
