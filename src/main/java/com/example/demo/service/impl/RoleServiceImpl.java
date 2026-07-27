package com.example.demo.service.impl;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.RoleMapping;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapping roleMapping;
    private final PermissionRepository permissionRepository;

    @Override
    public RoleResponse createRole(RoleCreateRequest request) {
        if(roleRepository.existsByName(request.name())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Set<Permission> permissions=resolvePermissions(request.permissionIds());
        Role role = roleMapping.toEntity(request);
        role.setPermissions(permissions);
        return roleMapping.toResponse(roleRepository.save(role));
    }

    @Override
    public List<RoleResponse> getRole() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapping::toResponse)
                .toList();
    }

    @Override
    public List<RoleSummaryResponse> getRoleOptions() {
        List<Role> roles= roleRepository.findAll();
        return roles.stream()
                .map(roleMapping::toResponseOptions)
                .toList();
    }


    private Set<Permission> resolvePermissions(Set<Long> permissionIds){
        List<Permission> found =permissionRepository.findAllById(permissionIds);
        if(found.size() != permissionIds.size()){
            Set<Long> foundIds = found.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());
            Set<Long> missing=new HashSet<>(permissionIds);
            missing.removeAll(foundIds);
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND,missing.toString());
        }
        return new HashSet<>(found);
    }
}
