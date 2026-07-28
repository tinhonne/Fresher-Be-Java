package com.example.demo.service.impl;

import com.example.demo.dto.request.permission.PermissionRequest;
import com.example.demo.dto.response.permission.PermissionResponse;
import com.example.demo.entity.Permission;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.PermissionMapping;
import com.example.demo.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapping permissionMapping;

    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionServiceImpl(permissionRepository, permissionMapping);
    }

    @Test
    void createSavesAndMapsPermission() {
        PermissionRequest request = new PermissionRequest("USER_VIEW", "View users");
        Permission permission = new Permission();
        Permission saved = Permission.builder().id(1L).code("USER_VIEW").description("View users").build();
        PermissionResponse response = new PermissionResponse(1L, "USER_VIEW", "View users");
        when(permissionMapping.toEntity(request)).thenReturn(permission);
        when(permissionRepository.save(permission)).thenReturn(saved);
        when(permissionMapping.toResponse(saved)).thenReturn(response);

        assertSame(response, service.createPermission(request));

        verify(permissionRepository).save(permission);
    }

    @Test
    void duplicateCreateIsRejectedBeforeMapping() {
        PermissionRequest request = new PermissionRequest("USER_VIEW", "View users");
        when(permissionRepository.existsBycode("USER_VIEW")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> service.createPermission(request));

        assertEquals(ErrorCode.PERMISSION_EXISTED, exception.getErrorCode());
        verifyNoInteractions(permissionMapping);
    }

    @Test
    void listUsesSortedScalarResponseQuery() {
        List<PermissionResponse> responses = List.of(
                new PermissionResponse(1L, "A", "First"),
                new PermissionResponse(2L, "B", "Second"));
        when(permissionRepository.findAllResponsesOrderById()).thenReturn(responses);

        assertSame(responses, service.getPermission());

        verify(permissionRepository).findAllResponsesOrderById();
        verifyNoInteractions(permissionMapping);
    }

    @Test
    void listReturnsEmptyList() {
        when(permissionRepository.findAllResponsesOrderById()).thenReturn(List.of());

        assertEquals(List.of(), service.getPermission());
    }

    @Test
    void methodsRequirePermissionManageAndHaveTransactions() throws NoSuchMethodException {
        Method create = PermissionServiceImpl.class.getMethod("createPermission", PermissionRequest.class);
        Method list = PermissionServiceImpl.class.getMethod("getPermission");

        assertEquals("hasAuthority('PERMISSION_MANAGE')", create.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAuthority('PERMISSION_MANAGE')", list.getAnnotation(PreAuthorize.class).value());
        assertFalse(create.getAnnotation(Transactional.class).readOnly());
        assertTrue(list.getAnnotation(Transactional.class).readOnly());
    }
}
