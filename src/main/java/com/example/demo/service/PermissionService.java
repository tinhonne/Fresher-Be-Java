package com.example.demo.service;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {
    /**
     * Creates a permission.
     *
     * @param permissionRequest the permission creation request
     * @return the created permission
     */
    PermissionResponse createPermission(PermissionRequest permissionRequest);
    List<PermissionResponse> getPermission();

    /**
     * Deletes an unassigned permission.
     *
     * @param id the permission identifier
     */
    void deletePermission(Long id);
}
