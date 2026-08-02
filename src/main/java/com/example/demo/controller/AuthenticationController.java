package com.example.demo.controller;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

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
     * @throws AppException if the user does not exist ({@code USER_NOT_FOUND}), is disabled ({@code USER_DISABLED}), or the credentials are invalid ({@code UNAUTHENTICATED})
     */
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authentication(@Valid @RequestBody AuthenticationRequest request){
        return ApiResponse.success(authenticationService.authentication(request));
    }
    /**
     * Verifies whether an access token is valid.
     *
     * @param request the token introspection request
     * @return the token validity result
     * @throws ParseException if the token cannot be parsed
     * @throws JOSEException if token verification cannot be performed
     */
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.success(authenticationService.introspect(request));
    }


}
