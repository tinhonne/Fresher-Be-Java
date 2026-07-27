package com.example.demo.service;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;

import java.util.List;


public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    List<RoleResponse> getRole();
    List<RoleSummaryResponse> getRoleOptions();

    //1 API update(PATCH), 1 API check Role này đang có User nào dùng, 1 API delete Role với điều kiện không có User
}
