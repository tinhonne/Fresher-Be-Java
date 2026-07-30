package com.example.demo.controller;

import com.example.demo.dto.request.user.PasswordUpdateRequest;
import com.example.demo.dto.request.user.UserCreateRequest;
import com.example.demo.dto.request.user.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.user.UserResponse;
import com.example.demo.dto.response.user.UserSummaryResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a user.
     *
     * @param request the user creation request
     * @return the created user summary
     */
    @PostMapping
    public ApiResponse<UserSummaryResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        return ApiResponse.success(userService.createUser(request));
    }

    /**
     * Returns users visible to the authenticated administrator or manager.
     *
     * @return visible user summaries
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
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id){
        return ApiResponse.success(userService.getUser(id));
    }

    /**
     * Returns the authenticated user.
     *
     * @return the authenticated user details
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
     */
    @PatchMapping("/{id}")
    public ApiResponse<UserSummaryResponse> updateUser(@PathVariable Long id,
                                                        @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param request the password update request
     * @return a successful empty response
     */
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ApiResponse.success(null);
    }
}
