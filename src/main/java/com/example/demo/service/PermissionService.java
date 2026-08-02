package com.example.demo.service;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.exception.AppException;

import java.util.List;

public interface PermissionService {
    /**
     * Creates a permission.
     *
     * @param permissionRequest the permission creation request
     * @return the created permission
     * @throws AppException if the permission already exists ({@code PERMISSION_EXISTED})
     */
    PermissionResponse createPermission(PermissionRequest permissionRequest);

    /**
     * Returns all permissions.
     *
     * @return all permissions
     */
    List<PermissionResponse> getPermission();

    /**
     * Deletes an unassigned permission.
     *
     * @param id the permission identifier
     * @throws AppException if the permission does not exist ({@code PERMISSION_NOT_FOUND}) or is assigned to a role ({@code PERMISSION_HAS_ROLE})
     */
    void deletePermission(Long id);
}
