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
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.constant.SecurityConstants.*;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapping userMapping;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    private static final Set<String> RESTRICTED_ROLES = Set.of(MANAGER_ROLE_NAME, ADMIN_ROLE_NAME);


    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Transactional
    @Override
    public UserSummaryResponse createUser(UserCreateRequest request) {
        if(userRepository.existsByUsername(request.username())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Set<Role> roles=request.roleIds().isEmpty() ?
                Set.of(getDefaultRole()):resolveRoles(request.roleIds());

        validateRoleAssignment(roles);
        User user = userMapping.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(roles);

        return userMapping.toSumamary(userRepository.save(user));
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryResponse> getListUser() {
        List<User> users;
        if (authenticationFacade.hasRole(ADMIN_ROLE)) {
            users = userRepository.findAllWithRolesOrderById();
        } else if (authenticationFacade.hasRole(MANAGER_ROLE)) {
            users = userRepository.findEmployeeScopedWithRolesOrderById(EMPLOYEE_ROLE_NAME, RESTRICTED_ROLES);
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return users.stream()
                .map(userMapping::toSumamary)
                .toList();
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Transactional(readOnly = true)
    @Override
    public UserResponse getUser(Long id) {
        User user;
        if (authenticationFacade.hasRole(ADMIN_ROLE)) {
            user = userRepository.findByIdWithRolesAndPermissions(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else if (authenticationFacade.hasRole(MANAGER_ROLE)) {
            user = userRepository.findEmployeeScopedByIdWithRolesAndPermissions(id, EMPLOYEE_ROLE_NAME, RESTRICTED_ROLES)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return userMapping.toResponse(user);
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public UserResponse getMe() {
        return userMapping.toResponse(getCurrentUser());
    }

    /** {@inheritDoc} */
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @Transactional
    @Override
    public UserSummaryResponse updateUser(Long id, UserUpdateRequest request) {
        if (request.name() == null && request.roleIds() == null) {
            throw new AppException(ErrorCode.INVALID_USER_UPDATE);
        }

        User user;
        if (authenticationFacade.hasRole(ADMIN_ROLE)) {
            user = userRepository.findByIdWithRolesAndPermissions(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (request.roleIds() != null && !request.roleIds().isEmpty()) {
                user.setRoles(resolveRoles(request.roleIds()));
            }
        } else if (authenticationFacade.hasRole(MANAGER_ROLE)) {
            user = userRepository.findEmployeeScopedByIdWithRolesAndPermissions(id, EMPLOYEE_ROLE_NAME, RESTRICTED_ROLES)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (request.roleIds() != null && !request.roleIds().isEmpty()) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (request.name() != null) {
            user.setName(request.name());
        }
        return userMapping.toSumamary(userRepository.save(user));
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void updatePassword(PasswordUpdateRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_OLD_PASSWORD);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
    }

    private User getCurrentUser() {
        return userRepository.findByUsername(authenticationFacade.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Role getDefaultRole(){
        return roleRepository.findByName(EMPLOYEE_ROLE_NAME)
                .orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    private Set<Role> resolveRoles(Set<Long> roleIds){
        List<Role> found=roleRepository.findAllById(roleIds);
        if(found.size()!=roleIds.size()){
            Set<Long> foundIds=found.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Set<Long> missing=new HashSet<>(roleIds);
            missing.removeAll(foundIds);
            throw new AppException(ErrorCode.ROLE_NOT_FOUND,missing.toString());
        }
        return new HashSet<>(found);
    }

    private void validateRoleAssignment(Set<Role> roles){
        boolean hasRestricted=roles.stream()
                .anyMatch(role -> RESTRICTED_ROLES.contains(role.getName()));

        if(hasRestricted){
            if(!authenticationFacade.hasAuthority(ASSIGN_RESTRICTED_ROLE_AUTHORITY)){
                throw new AppException(ErrorCode.FORBIDDEN_ASSIGN_ROLE);
            }

        }
    }
}
