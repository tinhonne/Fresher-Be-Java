package com.example.demo.service.impl;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.request.role.RolePermissionRequest;
import com.example.demo.dto.request.role.RoleUpdateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.RoleMapping;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final UserMapping userMapping;

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional
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

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional
    @Override
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        if (request.name() == null && request.description() == null && request.permissionIds() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        Role role = findRole(id);
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new AppException(ErrorCode.INVALID_ROLE_NAME);
            }
            if (roleRepository.existsByNameAndIdNot(request.name(), id)) {
                throw new AppException(ErrorCode.ROLE_EXISTED);
            }
            role.setName(request.name());
        }
        if (request.description() != null) {
            role.setDescription(request.description());
        }
        if (request.permissionIds() != null) {
            role.setPermissions(resolvePermissions(request.permissionIds()));
        }
        return roleMapping.toResponse(roleRepository.save(role));
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional
    @Override
    public RoleResponse addPermissions(Long roleId, RolePermissionRequest request) {
        Role role = findRole(roleId);
        role.getPermissions().addAll(resolvePermissions(request.permissionIds()));
        return roleMapping.toResponse(roleRepository.save(role));
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional
    @Override
    public void removePermissions(Long roleId, RolePermissionRequest request) {
        Role role = findRole(roleId);
        Set<Long> associatedIds = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        if (!associatedIds.containsAll(request.permissionIds())) {
            throw new AppException(ErrorCode.ROLE_PERMISSION_NOT_FOUND);
        }
        role.getPermissions().removeIf(permission -> request.permissionIds().contains(permission.getId()));
        roleRepository.save(role);
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional(readOnly = true)
    @Override
    public List<RoleResponse> getRole() {
        return roleRepository.findAllWithPermissionsOrderById().stream()
                .map(roleMapping::toResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional(readOnly = true)
    @Override
    public List<RoleSummaryResponse> getRoleOptions() {
        return roleRepository.findAllOptionsOrderById();
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional(readOnly = true)
    @Override
    public RoleResponse getRole(Long id) {
        return roleRepository.findByIdWithPermissions(id)
                .map(roleMapping::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryResponse> getRoleUsers(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        return userRepository.findByRoleIdWithRolesOrderById(id).stream()
                .map(userMapping::toSumamary)
                .toList();
    }


    private Role findRole(Long id) {
        return roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
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
