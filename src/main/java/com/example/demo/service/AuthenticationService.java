package com.example.demo.service;

import com.example.demo.dto.request.authentication.AuthenticationRequest;
import com.example.demo.dto.request.authentication.IntrospectRequest;
import com.example.demo.dto.request.authentication.LogoutRequest;
import com.example.demo.dto.response.authentication.AuthenticationResponse;
import com.example.demo.dto.response.authentication.IntrospectResponse;
import com.example.demo.exception.AppException;

public interface AuthenticationService {

  /**
   * Authenticates a user and issues an access token.
   *
   * @param request the authentication credentials
   * @return the authentication result and access token
   * @throws AppException if the user does not exist ({@code USER_NOT_FOUND}), is disabled ({@code
   *     USER_DISABLED}), or the credentials are invalid ({@code UNAUTHENTICATED})
   */
  AuthenticationResponse authentication(AuthenticationRequest request);

  /**
   * Verifies an access token.
   *
   * @param request the token introspection request
   * @return the token validity result
   */
  IntrospectResponse introspect(IntrospectRequest request);

  /**
   * Invalidates the access token provided in the logout request.
   *
   * <p>The token is first verified and then stored in the invalidated-token repository with its JWT
   * ID and expiration time.
   *
   * @param request the logout request containing the access token to invalidate
   * @throws AppException if the token is invalid
   */
  void logout(LogoutRequest request);
}
