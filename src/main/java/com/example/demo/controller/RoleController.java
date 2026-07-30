package com.example.demo.controller;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * Creates a role.
     *
     * @param roleCreateRequest the role creation request
     * @return the created role with its resource location
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
     */
    @PatchMapping("/{id}")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(id, request));
    }

    /**
     * Adds permissions to a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return the updated role
     */
    @PostMapping("/{roleId}/permissions")
    public ApiResponse<RoleResponse> addPermissions(
            @PathVariable Long roleId, @Valid @RequestBody RolePermissionRequest request) {
        return ApiResponse.success(roleService.addPermissions(roleId, request));
    }

    /**
     * Removes permissions from a role.
     *
     * @param roleId the role identifier
     * @param request the permission identifiers
     * @return a successful empty response
     */
    @PostMapping("/{roleId}/permissions/remove")
    public ApiResponse<Void> removePermissions(
            @PathVariable Long roleId, @Valid @RequestBody RolePermissionRequest request) {
        roleService.removePermissions(roleId, request);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRole(){
        return ApiResponse.success(roleService.getRole());
    }
    @GetMapping("/options")
    public ApiResponse<List<RoleSummaryResponse>> getRoleOptions(){
        return ApiResponse.success(roleService.getRoleOptions());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getRole(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRole(id));
    }

    @GetMapping("/{id}/users")
    public ApiResponse<List<UserSummaryResponse>> getRoleUsers(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleUsers(id));
    }
}
