package com.example.demo.service;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;

import java.util.List;

public interface UserService {
    /**
     * Creates a user.
     *
     * @param userCreateRequest the user creation request
     * @return the created user summary
     */
    UserSummaryResponse createUser(UserCreateRequest userCreateRequest);

    /**
     * Returns users visible to the authenticated administrator or manager.
     *
     * @return visible user summaries
     */
    List<UserSummaryResponse> getListUser();

    /**
     * Returns a visible user by identifier.
     *
     * @param id the user identifier
     * @return the user details
     */
    UserResponse getUser(Long id);

    /**
     * Returns the authenticated user.
     *
     * @return the authenticated user details
     */
    UserResponse getMe();

    /**
     * Partially updates a user visible to the authenticated administrator or manager.
     *
     * @param id the user identifier
     * @param request the update request
     * @return the updated user summary
     */
    UserSummaryResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * Changes the authenticated user's password.
     *
     * @param request the password update request
     */
    void updatePassword(PasswordUpdateRequest request);
}
