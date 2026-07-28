package com.example.demo.service.impl;

import com.example.demo.dto.request.role.RoleCreateRequest;
import com.example.demo.dto.response.role.RoleResponse;
import com.example.demo.dto.response.role.RoleSummaryResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.RoleMapping;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleMapping roleMapping;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapping userMapping;

    private RoleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoleServiceImpl(
                roleRepository, roleMapping, permissionRepository, userRepository, userMapping);
    }

    @Test
    void listUsesPermissionsQuery() {
        Role role = new Role();
        RoleResponse response = new RoleResponse(1L, "Admin", null, List.of());
        when(roleRepository.findAllWithPermissionsOrderById()).thenReturn(List.of(role));
        when(roleMapping.toResponse(role)).thenReturn(response);

        assertEquals(List.of(response), service.getRole());

        verify(roleRepository).findAllWithPermissionsOrderById();
    }

    @Test
    void optionsUseSummaryQueryWithoutMappingEntities() {
        List<RoleSummaryResponse> options = List.of(new RoleSummaryResponse(1L, "Admin"));
        when(roleRepository.findAllOptionsOrderById()).thenReturn(options);

        assertSame(options, service.getRoleOptions());

        verifyNoInteractions(roleMapping);
    }

    @Test
    void detailUsesPermissionsQuery() {
        Role role = new Role();
        RoleResponse response = new RoleResponse(2L, "Manager", null, List.of());
        when(roleRepository.findByIdWithPermissions(2L)).thenReturn(Optional.of(role));
        when(roleMapping.toResponse(role)).thenReturn(response);

        assertSame(response, service.getRole(2L));
    }

    @Test
    void missingDetailIsNotFound() {
        when(roleRepository.findByIdWithPermissions(9L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> service.getRole(9L));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void roleUsersVerifyRoleThenUseRolesQuery() {
        User user = new User();
        UserSummaryResponse response = new UserSummaryResponse(3L, "user", "User", true, false, List.of());
        when(roleRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findByRoleIdWithRolesOrderById(2L)).thenReturn(List.of(user));
        when(userMapping.toSumamary(user)).thenReturn(response);

        assertEquals(List.of(response), service.getRoleUsers(2L));

        verify(userRepository).findByRoleIdWithRolesOrderById(2L);
    }

    @Test
    void missingRoleDoesNotQueryUsers() {
        when(roleRepository.existsById(9L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> service.getRoleUsers(9L));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(userRepository, userMapping);
    }

    @Test
    void createResolvesPermissionsAndSavesRole() {
        RoleCreateRequest request = new RoleCreateRequest("Auditor", null, Set.of(2L, 1L));
        Permission first = Permission.builder().id(1L).code("A").build();
        Permission second = Permission.builder().id(2L).code("B").build();
        Role role = new Role();
        Role saved = new Role();
        RoleResponse response = new RoleResponse(4L, "Auditor", null, List.of());
        when(permissionRepository.findAllById(request.permissionIds())).thenReturn(List.of(first, second));
        when(roleMapping.toEntity(request)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(saved);
        when(roleMapping.toResponse(saved)).thenReturn(response);

        assertSame(response, service.createRole(request));

        verify(roleRepository).save(role);
        verify(roleRepository, never()).findAll();
    }
}
