package com.example.demo.service;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.exception.AppException;

import java.util.List;

public interface UserService {
    /**
     * Creates a user.
     *
     * @param userCreateRequest the user creation request
     * @return the created user summary
     * @throws AppException if the username exists ({@code USER_EXISTED}), the default or requested role does not exist ({@code ROLE_NOT_FOUND}), or assignment of a restricted role is forbidden ({@code FORBIDDEN_ASSIGN_ROLE})
     */
    UserSummaryResponse createUser(UserCreateRequest userCreateRequest);

    /**
     * Returns users visible to the authenticated administrator or manager.
     *
     * @return visible user summaries
     * @throws AppException if the authenticated user is neither an administrator nor a manager ({@code FORBIDDEN})
     */
    List<UserSummaryResponse> getListUser();

    /**
     * Returns a visible user by identifier.
     *
     * @param id the user identifier
     * @return the user details
     * @throws AppException if the user is not visible or does not exist ({@code USER_NOT_FOUND}), or access is forbidden ({@code FORBIDDEN})
     */
    UserResponse getUser(Long id);

    /**
     * Returns the authenticated user.
     *
     * @return the authenticated user details
     * @throws AppException if the authenticated user does not exist ({@code USER_NOT_FOUND})
     */
    UserResponse getMe();

    /**
     * Partially updates a user visible to the authenticated administrator or manager.
     *
     * @param id the user identifier
     * @param request the update request
     * @return the updated user summary
     * @throws AppException if no update is supplied ({@code INVALID_USER_UPDATE}), the user is not visible or does not exist ({@code USER_NOT_FOUND}), requested roles do not exist ({@code ROLE_NOT_FOUND}), or the update is forbidden ({@code FORBIDDEN})
     */
    UserSummaryResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * Changes the authenticated user's password.
     *
     * @param request the password update request
     * @throws AppException if the authenticated user does not exist ({@code USER_NOT_FOUND}), the old password is incorrect ({@code INCORRECT_OLD_PASSWORD}), or the new password matches the old password ({@code NEW_PASSWORD_SAME_AS_OLD})
     */
    void updatePassword(PasswordUpdateRequest request);
}
