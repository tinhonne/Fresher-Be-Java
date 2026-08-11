package com.example.demo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.config.properties.JwtProperties;
import com.example.demo.dto.request.authentication.IntrospectRequest;
import com.example.demo.dto.request.authentication.LogoutRequest;
import com.example.demo.entity.InvalidatedToken;
import com.example.demo.exception.AppException;
import com.example.demo.exception.error.AuthenticationError;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private InvalidatedTokenRepository invalidatedTokenRepository;
  @Mock private JwtDecoder jwtDecoder;

  private AuthenticationServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationServiceImpl(
            userRepository,
            passwordEncoder,
            new JwtProperties(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "issuer",
                Duration.ofHours(1)),
            invalidatedTokenRepository,
            jwtDecoder);
  }

  @Test
  void introspectReturnsTrueForValidToken() {
    when(jwtDecoder.decode("token")).thenReturn(jwt("id", Instant.now().plusSeconds(300)));

    assertThat(service.introspect(IntrospectRequest.builder().token("token").build()).isValid())
        .isTrue();
  }

  @Test
  void introspectReturnsFalseForJwtException() {
    when(jwtDecoder.decode("token")).thenThrow(new JwtException("invalid"));

    assertThat(service.introspect(IntrospectRequest.builder().token("token").build()).isValid())
        .isFalse();
  }

  @Test
  void introspectPropagatesDatabaseFailure() {
    when(jwtDecoder.decode("token"))
        .thenThrow(new DataAccessResourceFailureException("database unavailable"));

    assertThatThrownBy(() -> service.introspect(IntrospectRequest.builder().token("token").build()))
        .isInstanceOf(DataAccessResourceFailureException.class);
  }

  @Test
  void logoutStoresJwtIdAndExpiration() {
    Instant expiresAt = Instant.now().plusSeconds(300);
    when(jwtDecoder.decode("token")).thenReturn(jwt("id", expiresAt));

    service.logout(LogoutRequest.builder().token("token").build());

    ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
    verify(invalidatedTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo("id");
    assertThat(captor.getValue().getExpiryTime().toInstant())
        .isEqualTo(Instant.ofEpochMilli(expiresAt.toEpochMilli()));
  }

  @Test
  void logoutMapsJwtExceptionToUnauthenticated() {
    when(jwtDecoder.decode("token")).thenThrow(new JwtException("invalid"));

    assertThatThrownBy(() -> service.logout(LogoutRequest.builder().token("token").build()))
        .isInstanceOfSatisfying(
            AppException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(AuthenticationError.UNAUTHENTICATED));
  }

  @Test
  void logoutPropagatesDatabaseFailure() {
    when(jwtDecoder.decode("token")).thenReturn(jwt("id", Instant.now().plusSeconds(300)));
    when(invalidatedTokenRepository.save(any(InvalidatedToken.class)))
        .thenThrow(new DataAccessResourceFailureException("database unavailable"));

    assertThatThrownBy(() -> service.logout(LogoutRequest.builder().token("token").build()))
        .isInstanceOf(DataAccessResourceFailureException.class);
  }

  private Jwt jwt(String id, Instant expiresAt) {
    return new Jwt(
        "token",
        Instant.now(),
        expiresAt,
        Map.of("alg", "HS512"),
        Map.of("sub", "user", "jti", id, "exp", expiresAt));
  }
}
