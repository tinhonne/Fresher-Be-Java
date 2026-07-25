package com.example.demo.service.impl;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapping;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapping userMapping;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user=userMapping.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapping.toResponse(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Override
    public List<UserResponse> findUser() {
        log.info("in method");
        return userMapping.toUserResponselist(userRepository.findAll());
    }

//    @PostAuthorize("returnObject.username==authentication.name")
    @Override
    public UserResponse findUserbyId(long id) {
        User user=userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapping.toResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User byUsername = userRepository.findByUsername(name).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));


        return userMapping.toResponse(byUsername);
    }


}
