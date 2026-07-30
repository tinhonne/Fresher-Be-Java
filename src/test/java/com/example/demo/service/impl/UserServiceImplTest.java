package com.example.demo.service.impl;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
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
    @Mock
    private AuthenticationFacade authenticationFacade;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, userMapping, roleRepository, passwordEncoder,
                authenticationFacade);
    }

    @Test
    void createIsWriteTransactional() throws NoSuchMethodException {
        Method create = UserServiceImpl.class.getMethod("createUser", UserCreateRequest.class);

        assertEquals(false, create.getAnnotation(Transactional.class).readOnly());
    }

    @Test
    void createRequestUsesEmptyRolesWhenNull() {
        UserCreateRequest request = new UserCreateRequest("username", "password", "Name", null);

        assertEquals(java.util.Set.of(), request.roleIds());
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

        AppException exception = assertThrows(AppException.class, service::getListUser);

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
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

    @Test
    void updateAndPasswordAreWriteTransactional() throws NoSuchMethodException {
        Method update = UserServiceImpl.class.getMethod("updateUser", Long.class, UserUpdateRequest.class);
        Method password = UserServiceImpl.class.getMethod("updatePassword", PasswordUpdateRequest.class);

        assertFalse(update.getAnnotation(Transactional.class).readOnly());
        assertFalse(password.getAnnotation(Transactional.class).readOnly());
    }

    @Test
    void updateRequiresEffectiveField() {
        authenticate("admin", "ROLE_ADMIN", "USER_UPDATE");

        AppException exception = assertThrows(AppException.class,
                () -> service.updateUser(1L, new UserUpdateRequest(null, null)));

        assertEquals(ErrorCode.INVALID_USER_UPDATE, exception.getErrorCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void adminUpdatesNameAndReplacesNonemptyRoles() {
        authenticate("admin", "ROLE_ADMIN", "USER_UPDATE");
        User user = User.builder().name("Old").roles(Set.of()).build();
        Role role = Role.builder().id(3L).name("Employee").build();
        UserSummaryResponse response = new UserSummaryResponse(1L, "user", "New", true, false, List.of());
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(Set.of(3L))).thenReturn(List.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapping.toSumamary(user)).thenReturn(response);

        assertSame(response, service.updateUser(1L, new UserUpdateRequest("New", Set.of(3L))));
        assertEquals("New", user.getName());
        assertEquals(Set.of(role), user.getRoles());
    }

    @Test
    void adminEmptyRolesKeepsCurrentRoles() {
        authenticate("admin", "ROLE_ADMIN", "USER_UPDATE");
        Role role = Role.builder().id(3L).name("Employee").build();
        User user = User.builder().name("Old").roles(Set.of(role)).build();
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.updateUser(1L, new UserUpdateRequest(null, Set.of()));

        assertEquals(Set.of(role), user.getRoles());
        verifyNoInteractions(roleRepository);
    }

    @Test
    void managerUpdatesScopedNameWithEmptyRoles() {
        authenticate("manager", "ROLE_MANAGER", "USER_UPDATE");
        User user = User.builder().name("Old").build();
        when(userRepository.findEmployeeScopedByIdWithRolesAndPermissions(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.updateUser(2L, new UserUpdateRequest("New", Set.of()));

        assertEquals("New", user.getName());
        verify(userRepository).findEmployeeScopedByIdWithRolesAndPermissions(2L);
    }

    @Test
    void nonAdminOrManagerUpdateIsForbidden() {
        authenticate("employee", "ROLE_EMPLOYEE", "USER_UPDATE");

        AppException exception = assertThrows(AppException.class,
                () -> service.updateUser(2L, new UserUpdateRequest("New", null)));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verifyNoInteractions(userRepository, userMapping, roleRepository, passwordEncoder);
    }

    @Test
    void managerNonemptyRolesIsForbidden() {
        authenticate("manager", "ROLE_MANAGER", "USER_UPDATE");
        User user = new User();
        when(userRepository.findEmployeeScopedByIdWithRolesAndPermissions(2L)).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class,
                () -> service.updateUser(2L, new UserUpdateRequest("New", Set.of(3L))));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(userRepository, never()).save(user);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void managerUpdateOutOfScopeIsNotFound() {
        authenticate("manager", "ROLE_MANAGER", "USER_UPDATE");
        when(userRepository.findEmployeeScopedByIdWithRolesAndPermissions(7L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> service.updateUser(7L, new UserUpdateRequest("New", null)));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void ownerChangesPassword() {
        authenticate("owner", "ROLE_EMPLOYEE");
        User user = User.builder().id(4L).username("owner").password("encoded-old")
                .mustChangePassword(true).build();
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        service.updatePassword(new PasswordUpdateRequest("old-password", "new-password"));

        assertEquals("encoded-new", user.getPassword());
        assertFalse(user.isMustChangePassword());
        verify(userRepository).findByUsername("owner");
        verify(userRepository, never()).save(user);
    }

    @Test
    void wrongOldPasswordIsUnauthorized() {
        authenticate("owner", "ROLE_EMPLOYEE");
        User user = User.builder().id(4L).username("owner").password("encoded-old").build();
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-old", "encoded-old")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> service.updatePassword(new PasswordUpdateRequest("wrong-old", "new-password")));

        assertEquals(ErrorCode.INCORRECT_OLD_PASSWORD, exception.getErrorCode());
    }

    @Test
    void matchingNewPasswordIsRejected() {
        authenticate("owner", "ROLE_EMPLOYEE");
        User user = User.builder().id(4L).username("owner").password("encoded-old").build();
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("same-password", "encoded-old")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> service.updatePassword(new PasswordUpdateRequest("old-password", "same-password")));

        assertEquals(ErrorCode.NEW_PASSWORD_SAME_AS_OLD, exception.getErrorCode());
    }

    private void authenticate(String username, String... authorities) {
        lenient().when(authenticationFacade.getCurrentUsername()).thenReturn(username);
        for (String authority : authorities) {
            if (authority.startsWith("ROLE_")) {
                lenient().when(authenticationFacade.hasRole(authority.substring(5))).thenReturn(true);
            }
            lenient().when(authenticationFacade.hasAuthority(authority)).thenReturn(true);
        }
    }
}
