package com.example.demo.service;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.user.UserSummaryResponse;

public interface UserService {
    UserSummaryResponse createUser(UserCreateRequest userCreateRequest);
}
