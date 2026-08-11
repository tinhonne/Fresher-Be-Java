package com.example.demo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.UserError;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.authorization.AppRole;
import com.example.demo.security.context.AuthenticationFacade;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  private static final Set<AppRole> RESTRICTED = Set.of(AppRole.MANAGER, AppRole.ADMIN);
  @Mock private UserRepository userRepository;
  @Mock private UserMapping userMapping;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationFacade authenticationFacade;
  private UserServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new UserServiceImpl(userRepository, userMapping, passwordEncoder, authenticationFacade);
  }

  @Test
  void createDefaultsToEmployee() {
    UserCreateRequest request = new UserCreateRequest("employee", "password", "Employee", null);
    User user = new User();
    when(userMapping.toEntity(request)).thenReturn(user);
    when(passwordEncoder.encode("password")).thenReturn("encoded");
    when(userRepository.save(user)).thenReturn(user);
    service.createUser(request);
    assertEquals(Set.of(AppRole.EMPLOYEE), user.getRoles());
  }

  @Test
  void nonAdminCannotAssignRestrictedRole() {
    UserCreateRequest request =
        new UserCreateRequest("manager", "password", "Manager", Set.of(AppRole.MANAGER));
    AppException exception = assertThrows(AppException.class, () -> service.createUser(request));
    assertEquals(UserError.FORBIDDEN_ASSIGN_ROLE, exception.getErrorCode());
    verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void adminCanAssignRestrictedRole() {
    when(authenticationFacade.hasRole(AppRole.ADMIN)).thenReturn(true);
    UserCreateRequest request =
        new UserCreateRequest("manager", "password", "Manager", Set.of(AppRole.MANAGER));
    User user = new User();
    when(userMapping.toEntity(request)).thenReturn(user);
    when(userRepository.save(user)).thenReturn(user);
    service.createUser(request);
    assertEquals(Set.of(AppRole.MANAGER), user.getRoles());
  }

  @Test
  void managerListUsesStaticEmployeeScope() {
    lenient().when(authenticationFacade.hasRole(AppRole.MANAGER)).thenReturn(true);
    when(userRepository.findEmployeeScopedWithRolesOrderById(AppRole.EMPLOYEE, RESTRICTED))
        .thenReturn(List.of());
    assertEquals(List.of(), service.getListUser());
    verify(userRepository).findEmployeeScopedWithRolesOrderById(AppRole.EMPLOYEE, RESTRICTED);
  }

  @Test
  void employeeCannotListUsers() {
    AppException exception = assertThrows(AppException.class, service::getListUser);
    assertEquals(CommonError.FORBIDDEN, exception.getErrorCode());
  }

  @Test
  void adminGetsUserWithRoles() {
    when(authenticationFacade.hasRole(AppRole.ADMIN)).thenReturn(true);
    User user = User.builder().roles(Set.of(AppRole.ADMIN)).build();
    UserResponse response =
        new UserResponse(1L, "admin", "Admin", true, false, Set.of(AppRole.ADMIN), List.of());
    when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(user));
    when(userMapping.toResponse(user)).thenReturn(response);
    assertSame(response, service.getUser(1L));
  }

  @Test
  void managerCannotReplaceRoles() {
    lenient().when(authenticationFacade.hasRole(AppRole.MANAGER)).thenReturn(true);
    User user = User.builder().roles(Set.of(AppRole.EMPLOYEE)).build();
    when(userRepository.findEmployeeScopedByIdWithRoles(1L, AppRole.EMPLOYEE, RESTRICTED))
        .thenReturn(Optional.of(user));
    AppException exception =
        assertThrows(
            AppException.class,
            () -> service.updateUser(1L, new UserUpdateRequest("New", Set.of(AppRole.EMPLOYEE))));
    assertEquals(CommonError.FORBIDDEN, exception.getErrorCode());
  }

  @Test
  void ownerChangesPassword() {
    lenient().when(authenticationFacade.getCurrentUsername()).thenReturn("owner");
    User user =
        User.builder().username("owner").password("encoded-old").mustChangePassword(true).build();
    when(userRepository.findByUsername("owner")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("old-password", "encoded-old")).thenReturn(true);
    when(passwordEncoder.matches("new-password", "encoded-old")).thenReturn(false);
    when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");
    service.updatePassword(new PasswordUpdateRequest("old-password", "new-password"));
    assertEquals("encoded-new", user.getPassword());
    assertFalse(user.isMustChangePassword());
    verifyNoInteractions(userMapping);
  }
}
