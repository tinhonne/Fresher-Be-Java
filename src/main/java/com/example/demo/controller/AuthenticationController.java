package com.example.demo.controller;

import com.example.demo.dto.request.authentication.AuthenticationRequest;
import com.example.demo.dto.request.authentication.IntrospectRequest;
import com.example.demo.dto.request.authentication.LogoutRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.authentication.AuthenticationResponse;
import com.example.demo.dto.response.authentication.IntrospectResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.CommonError;
import com.example.demo.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
  private final AuthenticationService authenticationService;

  /**
   * Authenticates a user and returns an access token.
   *
   * @param request the authentication credentials
   * @return the authentication result
   * @throws AppException if the user does not exist ({@code USER_NOT_FOUND}), is disabled ({@code
   *     USER_DISABLED}), or the credentials are invalid ({@code UNAUTHENTICATED})
   */
  @PostMapping("/token")
  ApiResponse<AuthenticationResponse> authentication(
      @Valid @RequestBody AuthenticationRequest request) {
    return ApiResponse.success(authenticationService.authentication(request));
  }

  /**
   * Verifies whether an access token is valid.
   *
   * @param request the token introspection request
   * @return the token validity result
   */
  @PostMapping("/introspect")
  ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
    return ApiResponse.success(authenticationService.introspect(request));
  }

  /**
   * Logs out the current user by invalidating the provided access token.
   *
   * <p>The token is verified before being stored in the invalidated-token repository. Once
   * invalidated, the token can no longer be used for authenticated requests.
   *
   * @param request the logout request containing the access token to invalidate
   * @return an API response indicating that the logout operation was successful
   * @throws AppException if the token is invalid
   */
  @PostMapping("/logout")
  ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
    authenticationService.logout(request);
    return ApiResponse.<Void>builder()
        .code(CommonError.SUCCESS.getCode())
        .message(CommonError.SUCCESS.getMessage())
        .build();
  }
}
