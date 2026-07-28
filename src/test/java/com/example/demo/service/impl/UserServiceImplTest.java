package com.example.demo.service.impl;

import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapping userMapping;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, userMapping, roleRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminListUsesAllUsersQuery() {
        authenticate("admin", "ROLE_ADMIN", "USER_VIEW");
        User user = new User();
        UserSummaryResponse response = new UserSummaryResponse(1L, "admin", "Admin", true, false, List.of());
        when(userRepository.findAllWithRolesOrderById()).thenReturn(List.of(user));
        when(userMapping.toSumamary(user)).thenReturn(response);

        assertEquals(List.of(response), service.getListUser());

        verify(userRepository).findAllWithRolesOrderById();
        verify(userRepository, never()).findEmployeeScopedWithRolesOrderById();
    }

    @Test
    void managerListUsesEmployeeScopedQuery() {
        authenticate("manager", "ROLE_MANAGER", "USER_VIEW");
        when(userRepository.findEmployeeScopedWithRolesOrderById()).thenReturn(List.of());

        assertEquals(List.of(), service.getListUser());

        verify(userRepository).findEmployeeScopedWithRolesOrderById();
        verify(userRepository, never()).findAllWithRolesOrderById();
    }

    @Test
    void otherRoleWithUserViewIsDenied() {
        authenticate("employee", "ROLE_EMPLOYEE", "USER_VIEW");

        assertThrows(AccessDeniedException.class, service::getListUser);

        verifyNoInteractions(userRepository, userMapping);
    }

    @Test
    void managerOutOfScopeUserIsNotFound() {
        authenticate("manager", "ROLE_MANAGER", "USER_VIEW");
        when(userRepository.findEmployeeScopedByIdWithRolesAndPermissions(7L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> service.getUser(7L));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findEmployeeScopedByIdWithRolesAndPermissions(7L);
        verify(userRepository, never()).findByIdWithRolesAndPermissions(7L);
    }

    @Test
    void adminGetsAnyUserWithDetailedGraphQuery() {
        authenticate("admin", "ROLE_ADMIN", "USER_VIEW");
        User user = new User();
        UserResponse response = new UserResponse(7L, "user", "User", true, false, List.of(), List.of());
        when(userRepository.findByIdWithRolesAndPermissions(7L)).thenReturn(Optional.of(user));
        when(userMapping.toResponse(user)).thenReturn(response);

        assertSame(response, service.getUser(7L));
    }

    @Test
    void meUsesAuthenticatedUsernameAndDetailedMapping() {
        authenticate("employee", "ROLE_EMPLOYEE");
        User user = new User();
        UserResponse response = new UserResponse(3L, "employee", "Employee", true, false, List.of(), List.of());
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(userMapping.toResponse(user)).thenReturn(response);

        assertSame(response, service.getMe());

        verify(userRepository).findByUsername("employee");
    }

    private void authenticate(String username, String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList()));
    }
}
