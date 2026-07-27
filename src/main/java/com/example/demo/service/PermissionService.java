package com.example.demo.service;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest permissionRequest);
    List<PermissionResponse> getPermission();
}
