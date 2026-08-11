package com.example.demo.service.impl;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.error.UserError;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.authorization.AppRole;
import com.example.demo.security.context.AuthenticationFacade;
import com.example.demo.service.UserService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
  private static final Set<AppRole> RESTRICTED_ROLES = Set.of(AppRole.MANAGER, AppRole.ADMIN);
  private final UserRepository userRepository;
  private final UserMapping userMapping;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationFacade authenticationFacade;

  @Transactional
  @Override
  public UserSummaryResponse createUser(UserCreateRequest request) {
    if (userRepository.existsByUsername(request.username()))
      throw new AppException(UserError.USER_EXISTED);
    Set<AppRole> roles =
        request.roles().isEmpty() ? Set.of(AppRole.EMPLOYEE) : Set.copyOf(request.roles());
    validateRoleAssignment(roles);
    User user = userMapping.toEntity(request);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setEnabled(true);
    user.setRoles(roles);
    return userMapping.toSumamary(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserSummaryResponse> getListUser() {
    List<User> users;
    if (authenticationFacade.hasRole(AppRole.ADMIN))
      users = userRepository.findAllWithRolesOrderById();
    else if (authenticationFacade.hasRole(AppRole.MANAGER))
      users =
          userRepository.findEmployeeScopedWithRolesOrderById(AppRole.EMPLOYEE, RESTRICTED_ROLES);
    else throw new AppException(CommonError.FORBIDDEN);
    return users.stream().map(userMapping::toSumamary).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponse getUser(Long id) {
    User user;
    if (authenticationFacade.hasRole(AppRole.ADMIN))
      user =
          userRepository
              .findByIdWithRoles(id)
              .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
    else if (authenticationFacade.hasRole(AppRole.MANAGER))
      user =
          userRepository
              .findEmployeeScopedByIdWithRoles(id, AppRole.EMPLOYEE, RESTRICTED_ROLES)
              .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
    else throw new AppException(CommonError.FORBIDDEN);
    return userMapping.toResponse(user);
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponse getMe() {
    return userMapping.toResponse(getCurrentUser());
  }

  @Transactional
  @Override
  public UserSummaryResponse updateUser(Long id, UserUpdateRequest request) {
    if (request.name() == null && request.roles() == null)
      throw new AppException(UserError.INVALID_USER_UPDATE);
    User user;
    if (authenticationFacade.hasRole(AppRole.ADMIN)) {
      user =
          userRepository
              .findByIdWithRoles(id)
              .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
      if (request.roles() != null && !request.roles().isEmpty()) {
        validateRoleAssignment(request.roles());
        user.setRoles(Set.copyOf(request.roles()));
      }
    } else if (authenticationFacade.hasRole(AppRole.MANAGER)) {
      user =
          userRepository
              .findEmployeeScopedByIdWithRoles(id, AppRole.EMPLOYEE, RESTRICTED_ROLES)
              .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
      if (request.roles() != null && !request.roles().isEmpty())
        throw new AppException(CommonError.FORBIDDEN);
    } else throw new AppException(CommonError.FORBIDDEN);
    if (request.name() != null) user.setName(request.name());
    return userMapping.toSumamary(userRepository.save(user));
  }

  @Transactional
  @Override
  public void updatePassword(PasswordUpdateRequest request) {
    User user = getCurrentUser();
    if (!passwordEncoder.matches(request.oldPassword(), user.getPassword()))
      throw new AppException(UserError.INCORRECT_OLD_PASSWORD);
    if (passwordEncoder.matches(request.newPassword(), user.getPassword()))
      throw new AppException(UserError.NEW_PASSWORD_SAME_AS_OLD);
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    user.setMustChangePassword(false);
  }

  private User getCurrentUser() {
    return userRepository
        .findByUsername(authenticationFacade.getCurrentUsername())
        .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
  }

  private void validateRoleAssignment(Set<AppRole> roles) {
    if (roles.stream().anyMatch(RESTRICTED_ROLES::contains)
        && !authenticationFacade.hasRole(AppRole.ADMIN))
      throw new AppException(UserError.FORBIDDEN_ASSIGN_ROLE);
  }
}
