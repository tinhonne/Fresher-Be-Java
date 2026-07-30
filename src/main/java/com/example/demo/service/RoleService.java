package com.example.demo.service;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;

import java.util.List;


public interface RoleService {
    /**
     * Creates a role.
     *
     * @param request the role creation request
     * @return the created role
     */
    RoleResponse createRole(RoleCreateRequest request);

    /**
     * Partially updates a role.
     *
     * @param id the role identifier
     * @param request the role update request
     * @return the updated role
     */
    RoleResponse updateRole(Long id, RoleUpdateRequest request);

    /**
     * Adds permissions to a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return the updated role
     */
    RoleResponse addPermissions(Long roleId, RolePermissionRequest request);

    /**
     * Removes permissions from a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     */
    void removePermissions(Long roleId, RolePermissionRequest request);

    List<RoleResponse> getRole();
    List<RoleSummaryResponse> getRoleOptions();
    RoleResponse getRole(Long id);
    List<UserSummaryResponse> getRoleUsers(Long id);
}
