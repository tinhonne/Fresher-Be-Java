package com.example.demo.service.impl;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.entity.Permission;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PermissionMapping;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapping permissionMapping;
    private final RoleRepository roleRepository;

    /**
     * Creates a permission.
     *
     * @param permissionRequest the permission creation request
     * @return the created permission
     */
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @Transactional
    @Override
    public PermissionResponse createPermission(PermissionRequest permissionRequest) {

        if(permissionRepository.existsBycode(permissionRequest.code())){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        Permission save = permissionRepository.save(permissionMapping.toEntity(permissionRequest));
        return permissionMapping.toResponse(save);
    }

    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @Transactional(readOnly = true)
    @Override
    public List<PermissionResponse> getPermission() {
        return permissionRepository.findAllResponsesOrderById();
    }

    /**
     * Deletes an unassigned permission.
     *
     * @param id the permission identifier
     */
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @Transactional
    @Override
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        if (roleRepository.existsByPermissionId(id)) {
            throw new AppException(ErrorCode.PERMISSION_HAS_ROLE);
        }
        permissionRepository.delete(permission);
    }

}
