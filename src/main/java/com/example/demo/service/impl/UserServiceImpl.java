package com.example.demo.service.impl;

import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapping userMapping;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ROLE_NAME = "Employee";
    private static final Set<String> RESTRICTED_ROLES = Set.of("Manager", "Admin");


    @PreAuthorize("hasAuthority('USER_CREATE')")
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

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryResponse> getListUser() {
        List<User> users;
        if (hasRole("ADMIN")) {
            users = userRepository.findAllWithRolesOrderById();
        } else if (hasRole("MANAGER")) {
            users = userRepository.findEmployeeScopedWithRolesOrderById();
        } else {
            throw new AccessDeniedException("Access denied");
        }
        return users.stream()
                .map(userMapping::toSumamary)
                .toList();
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
    @Transactional(readOnly = true)
    @Override
    public UserResponse getUser(Long id) {
        User user;
        if (hasRole("ADMIN")) {
            user = userRepository.findByIdWithRolesAndPermissions(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else if (hasRole("MANAGER")) {
            user = userRepository.findEmployeeScopedByIdWithRolesAndPermissions(id)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            throw new AccessDeniedException("Access denied");
        }
        return userMapping.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapping.toResponse(user);
    }


    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    private Role getDefaultRole(){
        return roleRepository.findByName(DEFAULT_ROLE_NAME)
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean canAssignRestricted= authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("USER_ASSIGN_RESTRICTED_ROLE"));

            if(!canAssignRestricted){
                throw new AppException(ErrorCode.FORBIDDEN_ASSIGN_ROLE);
            }

        }
    }
}
