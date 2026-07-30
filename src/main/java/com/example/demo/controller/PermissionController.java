package com.example.demo.controller;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Creates a permission.
     *
     * @param permissionRequest the permission creation request
     * @return the created permission
     */
    @PostMapping
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest permissionRequest){
      return ApiResponse.success(permissionService.createPermission(permissionRequest));
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getPermission(){
        return ApiResponse.success((permissionService.getPermission()));
    }

    /**
     * Deletes a permission.
     *
     * @param id the permission identifier
     * @return a successful empty response
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null);
    }
}
