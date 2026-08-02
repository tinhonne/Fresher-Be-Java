package com.example.demo.controller;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a user.
     *
     * @param request the user creation request
     * @return the created user summary
     * @throws AppException if the username exists ({@code USER_EXISTED}), a role does not exist ({@code ROLE_NOT_FOUND}), or restricted-role assignment is forbidden ({@code FORBIDDEN_ASSIGN_ROLE})
     */
    @PostMapping
    public ApiResponse<UserSummaryResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        return ApiResponse.success(userService.createUser(request));
    }

    /**
     * Returns users visible to the authenticated administrator or manager.
     *
     * @return visible user summaries
     * @throws AppException if access is forbidden ({@code FORBIDDEN})
     */
    @GetMapping
    public ApiResponse<List<UserSummaryResponse>> getListUser(){
        return ApiResponse.success(userService.getListUser());
    }

    /**
     * Returns a visible user by identifier.
     *
     * @param id the user identifier
     * @return the user details
     * @throws AppException if the user is not visible or does not exist ({@code USER_NOT_FOUND}), or access is forbidden ({@code FORBIDDEN})
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@Positive @PathVariable Long id){
        return ApiResponse.success(userService.getUser(id));
    }

    /**
     * Returns the authenticated user.
     *
     * @return the authenticated user details
     * @throws AppException if the authenticated user does not exist ({@code USER_NOT_FOUND})
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(){
        return ApiResponse.success(userService.getMe());
    }

    /**
     * Partially updates a user.
     *
     * @param id the user identifier
     * @param request the update request
     * @return the updated user summary
     * @throws AppException if the update is empty ({@code INVALID_USER_UPDATE}), the user is not visible or does not exist ({@code USER_NOT_FOUND}), a role does not exist ({@code ROLE_NOT_FOUND}), or the update is forbidden ({@code FORBIDDEN})
     */
    @PatchMapping("/{id}")
    public ApiResponse<UserSummaryResponse> updateUser(@Positive @PathVariable Long id,
                                                        @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param request the password update request
     * @return a successful empty response
     * @throws AppException if the user does not exist ({@code USER_NOT_FOUND}), the old password is incorrect ({@code INCORRECT_OLD_PASSWORD}), or the new password matches it ({@code NEW_PASSWORD_SAME_AS_OLD})
     */
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ApiResponse.success(null);
    }
}
