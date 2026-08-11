package com.example.demo.service.impl;

import static com.example.demo.constant.SecurityConstants.*;

import com.example.demo.config.properties.JwtProperties;
import com.example.demo.dto.request.authentication.AuthenticationRequest;
import com.example.demo.dto.request.authentication.IntrospectRequest;
import com.example.demo.dto.request.authentication.LogoutRequest;
import com.example.demo.dto.response.authentication.AuthenticationResponse;
import com.example.demo.dto.response.authentication.IntrospectResponse;
import com.example.demo.entity.InvalidatedToken;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.AuthenticationError;
import com.example.demo.exception.error.UserError;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtProperties jwtProperties;

  private final InvalidatedTokenRepository invalidatedTokenRepository;

  private final JwtDecoder jwtDecoder;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public AuthenticationResponse authentication(AuthenticationRequest request) {
    var user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));

    if (!user.isEnabled()) {
      throw new AppException(UserError.USER_DISABLED);
    }

    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

    if (!authenticated) {
      throw new AppException(AuthenticationError.UNAUTHENTICATED);
    }

    var token = generateToken(user);

    return AuthenticationResponse.builder()
        .token(token)
        .authenticated(true)
        .mustChangePassword(user.isMustChangePassword())
        .build();
  }

  /**
   * Generates an HS512-signed access token containing the user's roles.
   *
   * @param user the authenticated user
   * @return the serialized access token
   * @throws RuntimeException if token signing cannot be performed
   */
  public String generateToken(User user) {

    JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

    JWTClaimsSet jwtClaimsSet =
        new JWTClaimsSet.Builder()
            .subject(user.getUsername())
            .issuer(jwtProperties.issuer())
            .issueTime(new Date())
            .expirationTime(Date.from(Instant.now().plus(jwtProperties.expiration())))
            .jwtID(UUID.randomUUID().toString())
            .claim(SCOPE_CLAIM, buildScope(user))
            .build();

    Payload payload = new Payload(jwtClaimsSet.toJSONObject());

    JWSObject jwsObject = new JWSObject(jwsHeader, payload);

    try {
      jwsObject.sign(new MACSigner(jwtProperties.signerKeyBytes()));
      return jwsObject.serialize();
    } catch (JOSEException e) {
      log.error("Cannot create Token", e);
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public IntrospectResponse introspect(IntrospectRequest request) {
    boolean isValid = true;
    try {
      jwtDecoder.decode(request.getToken());
    } catch (JwtException e) {
      isValid = false;
    }
    return IntrospectResponse.builder().valid(isValid).build();
  }

  /** {@inheritDoc} */
  @Override
  public void logout(LogoutRequest request) {
    try {
      var jwt = jwtDecoder.decode(request.getToken());
      InvalidatedToken invalidatedToken =
          InvalidatedToken.builder()
              .id(jwt.getId())
              .expiryTime(Date.from(jwt.getExpiresAt()))
              .build();
      invalidatedTokenRepository.save(invalidatedToken);
    } catch (JwtException e) {
      throw new AppException(AuthenticationError.UNAUTHENTICATED);
    }
  }

  private String buildScope(User user) {
    if (CollectionUtils.isEmpty(user.getRoles())) {
      return EMPTY_SCOPE;
    }

    Stream<String> roleScopes = user.getRoles().stream().map(role -> ROLE_PREFIX + role.name());

    return roleScopes.distinct().collect(Collectors.joining(SCOPE_DELIMITER));
  }
}
