package com.example.demo.controller;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest roleCreateRequest){
        return ApiResponse.success(roleService.createRole(roleCreateRequest));
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRole(){
        return ApiResponse.success(roleService.getRole());
    }
    @GetMapping("/options")
    public ApiResponse<List<RoleSummaryResponse>> getRoleOptions(){
        return ApiResponse.success(roleService.getRoleOptions());
    }
}
