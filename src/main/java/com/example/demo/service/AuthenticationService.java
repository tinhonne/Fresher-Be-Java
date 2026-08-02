package com.example.demo.service;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.exception.AppException;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;


public interface AuthenticationService {

    /**
     * Authenticates a user and issues an access token.
     *
     * @param request the authentication credentials
     * @return the authentication result and access token
     * @throws AppException if the user does not exist ({@code USER_NOT_FOUND}), is disabled ({@code USER_DISABLED}), or the credentials are invalid ({@code UNAUTHENTICATED})
     */
    AuthenticationResponse authentication(AuthenticationRequest request);

    /**
     * Verifies a token's signature and expiration.
     *
     * @param request the token introspection request
     * @return the token validity result
     * @throws JOSEException if token verification cannot be performed
     * @throws ParseException if the token cannot be parsed
     */
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
}
