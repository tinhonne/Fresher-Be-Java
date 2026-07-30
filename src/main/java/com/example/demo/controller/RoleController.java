package com.example.demo.controller;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * Creates a role.
     *
     * @param roleCreateRequest the role creation request
     * @return the created role with its resource location
     * @throws AppException if the role already exists ({@code ROLE_EXISTED}) or a requested permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleCreateRequest roleCreateRequest){
        RoleResponse role = roleService.createRole(roleCreateRequest);
        return ResponseEntity.created(URI.create("/roles/" + role.id()))
                .body(ApiResponse.success(role));
    }

    /**
     * Partially updates a role.
     *
     * @param id the role identifier
     * @param request the role update request
     * @return the updated role
     * @throws AppException if the update is invalid ({@code INVALID_INPUT} or {@code INVALID_ROLE_NAME}), the role does not exist ({@code ROLE_NOT_FOUND}), the name exists ({@code ROLE_EXISTED}), or a permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    @PatchMapping("/{id}")
    public ApiResponse<RoleResponse> updateRole(
            @Positive @PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(id, request));
    }

    /**
     * Adds permissions to a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return the updated role
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND}) or a requested permission does not exist ({@code PERMISSION_NOT_FOUND})
     */
    @PostMapping("/{roleId}/permissions")
    public ApiResponse<RoleResponse> addPermissions(
            @Positive @PathVariable Long roleId, @Valid @RequestBody RolePermissionRequest request) {
        return ApiResponse.success(roleService.addPermissions(roleId, request));
    }

    /**
     * Removes permissions from a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return a successful empty response
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND}) or a requested permission is not assigned ({@code ROLE_PERMISSION_NOT_FOUND})
     */
    @PostMapping("/{roleId}/permissions/remove")
    public ApiResponse<Void> removePermissions(
            @Positive @PathVariable Long roleId, @Valid @RequestBody RolePermissionRequest request) {
        roleService.removePermissions(roleId, request);
        return ApiResponse.success(null);
    }

    /**
     * Returns all roles.
     *
     * @return all roles
     */
    @GetMapping
    public ApiResponse<List<RoleResponse>> getRole(){
        return ApiResponse.success(roleService.getRole());
    }
    /**
     * Returns role options for selection lists.
     *
     * @return role summaries
     */
    @GetMapping("/options")
    public ApiResponse<List<RoleSummaryResponse>> getRoleOptions(){
        return ApiResponse.success(roleService.getRoleOptions());
    }

    /**
     * Returns a role by identifier.
     *
     * @param id the role identifier
     * @return the role details
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND})
     */
    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getRole(@Positive @PathVariable Long id) {
        return ApiResponse.success(roleService.getRole(id));
    }

    /**
     * Returns users assigned to a role.
     *
     * @param id the role identifier
     * @return assigned users
     * @throws AppException if the role does not exist ({@code ROLE_NOT_FOUND})
     */
    @GetMapping("/{id}/users")
    public ApiResponse<List<UserSummaryResponse>> getRoleUsers(@Positive @PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleUsers(id));
    }
}
