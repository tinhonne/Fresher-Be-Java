package com.example.demo.controller;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Creates a permission.
     *
     * @param permissionRequest the permission creation request
     * @return the created permission
     * @throws AppException if the permission already exists ({@code PERMISSION_EXISTED})
     */
    @PostMapping
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest permissionRequest){
      return ApiResponse.success(permissionService.createPermission(permissionRequest));
    }

    /**
     * Returns all permissions.
     *
     * @return all permissions
     */
    @GetMapping
    public ApiResponse<List<PermissionResponse>> getPermission(){
        return ApiResponse.success((permissionService.getPermission()));
    }

    /**
     * Deletes a permission.
     *
     * @param id the permission identifier
     * @return a successful empty response
     * @throws AppException if the permission does not exist ({@code PERMISSION_NOT_FOUND}) or is assigned to a role ({@code PERMISSION_HAS_ROLE})
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@Positive @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null);
    }
}
