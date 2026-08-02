package com.example.demo.service;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.exception.AppException;

import java.util.List;


public interface RoleService {
    /**
     * Creates a role.
     *
     * @param request the role creation request
     * @return the created role
     * @throws AppException if the role already exists ({@code ROLE_EXISTED}) or a requested permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    RoleResponse createRole(RoleCreateRequest request);

    /**
     * Partially updates a role.
     *
     * @param id the role identifier
     * @param request the role update request
     * @return the updated role
     * @throws AppException if no update is supplied ({@code INVALID_INPUT}), the role does not exist ({@code ROLE_NOT_FOUND}), the role name is blank ({@code INVALID_ROLE_NAME}) or already exists ({@code ROLE_EXISTED}), or a requested permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    RoleResponse updateRole(Long id, RoleUpdateRequest request);

    /**
     * Adds permissions to a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return the updated role
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND}) or a requested permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    RoleResponse addPermissions(Long roleId, RolePermissionRequest request);

    /**
     * Removes permissions from a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND}) or a requested permission is not assigned to it ({@code ROLE_PERMISSION_NOT_FOUND})
     */
    void removePermissions(Long roleId, RolePermissionRequest request);

    /**
     * Returns all roles with their permissions.
     *
     * @return all roles
     */
    List<RoleResponse> getRole();

    /**
     * Returns role options suitable for selection lists.
     *
     * @return role summaries
     */
    List<RoleSummaryResponse> getRoleOptions();

    /**
     * Returns a role by identifier.
     *
     * @param id the role identifier
     * @return the role details
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND})
     */
    RoleResponse getRole(Long id);

    /**
     * Returns users assigned to a role.
     *
     * @param id the role identifier
     * @return assigned user summaries
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND})
     */
    List<UserSummaryResponse> getRoleUsers(Long id);
}
