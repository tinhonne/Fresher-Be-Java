package com.example.demo.service.impl;

import com.example.demo.config.properties.JwtProperties;
import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.entity.Permission;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.demo.constant.SecurityConstants.ROLE_PREFIX;
import static com.example.demo.constant.SecurityConstants.SCOPE_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtProperties jwtProperties;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse authentication(AuthenticationRequest request){
        var user=userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!user.isEnabled()){
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token= generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .mustChangePassword(user.isMustChangePassword())
                .build();

    }
    /**
     * Generates a signed access token containing the user's roles and permissions.
     *
     * @param user the authenticated user
     * @return the serialized access token
     * @throws RuntimeException if token signing cannot be performed
     */
    public String generateToken(User user){

        JWSHeader jwsHeader=new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet= new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.issuer())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(jwtProperties.expiration())))
                .claim(SCOPE_CLAIM,buildScope(user))
                .build();

        Payload payload=new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject=new JWSObject(jwsHeader,payload);

        try {
            jwsObject.sign(new MACSigner(jwtProperties.signerKeyBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create Token",e);
            throw new RuntimeException(e);
        }
    }
    /** {@inheritDoc} */
    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token=request.getToken();

        JWSVerifier verifier=new MACVerifier(jwtProperties.signerKeyBytes());

        SignedJWT signedJWT=SignedJWT.parse(token);
        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified=signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();

    }

    private String buildScope(User user) {
        if (CollectionUtils.isEmpty(user.getRoles())) {
            return "";
        }

        Stream<String> roleScopes = user.getRoles().stream()
                .map(role -> ROLE_PREFIX + role.getName().toUpperCase());

        Stream<String> permissionScopes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode);

        return Stream.concat(roleScopes, permissionScopes)
                .distinct()
                .collect(Collectors.joining(" "));
    }
}
