package com.example.demo.service;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;

import java.util.List;


public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    List<RoleResponse> getRole();
    List<RoleSummaryResponse> getRoleOptions();
    RoleResponse getRole(Long id);
    List<UserSummaryResponse> getRoleUsers(Long id);
}
