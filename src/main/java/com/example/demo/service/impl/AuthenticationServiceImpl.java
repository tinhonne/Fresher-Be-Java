package com.example.demo.service.impl;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Override
    public AuthenticationResponse authentication(AuthenticationRequest request){
        var user=userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token=genarateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

    }
    public String genarateToken(User user){

        JWSHeader jwsHeader=new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet= new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("test.vn")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(user))
                .build();

        Payload payload=new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject=new JWSObject(jwsHeader,payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create Token",e);
            throw new RuntimeException(e);
        }
    }
    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token=request.getToken();

        JWSVerifier verifier=new MACVerifier(signerKey.getBytes());

        SignedJWT signedJWT=SignedJWT.parse(token);
        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified=signedJWT.verify(verifier);
        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();

    }

    private String buildScope(User user){
        StringJoiner stringJoiner=new StringJoiner(" ");
//        if(user.getRole()!=null){
//          stringJoiner.add(user.getRole().name());
//        }
        return stringJoiner.toString();
    }

}
