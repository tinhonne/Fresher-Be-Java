package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.config.properties.JwtProperties;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.security.jwt.AccessTokenPolicyValidator;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtConfigTest {

  private static final String KEY =
      "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  private static final String OTHER_KEY =
      "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
  private static final String ISSUER = "test-issuer";

  @Mock private InvalidatedTokenRepository invalidatedTokenRepository;

  private JwtDecoder decoder;

  @BeforeEach
  void setUp() {
    JwtProperties properties = new JwtProperties(KEY, ISSUER, Duration.ofHours(1));
    decoder =
        new JwtConfig()
            .jwtDecoder(properties, new AccessTokenPolicyValidator(invalidatedTokenRepository));
  }

  @Test
  void acceptsValidToken() throws Exception {
    assertThat(decoder.decode(token(KEY, ISSUER, Instant.now().plusSeconds(300), null, "id")))
        .isNotNull();
  }

  @Test
  void rejectsWrongSignature() throws Exception {
    assertInvalid(token(OTHER_KEY, ISSUER, Instant.now().plusSeconds(300), null, "id"));
  }

  @Test
  void rejectsExpiredToken() throws Exception {
    assertInvalid(token(KEY, ISSUER, Instant.now().minusSeconds(120), null, "id"));
  }

  @Test
  void rejectsWrongIssuer() throws Exception {
    assertInvalid(token(KEY, "wrong", Instant.now().plusSeconds(300), null, "id"));
  }

  @Test
  void rejectsFutureNotBefore() throws Exception {
    assertInvalid(
        token(KEY, ISSUER, Instant.now().plusSeconds(600), Instant.now().plusSeconds(300), "id"));
  }

  @Test
  void rejectsMissingJwtId() throws Exception {
    assertInvalid(token(KEY, ISSUER, Instant.now().plusSeconds(300), null, null));
  }

  @Test
  void rejectsBlankJwtId() throws Exception {
    assertInvalid(token(KEY, ISSUER, Instant.now().plusSeconds(300), null, " "));
  }

  @Test
  void rejectsMissingExpiration() throws Exception {
    assertInvalid(token(KEY, ISSUER, null, null, "id"));
  }

  @Test
  void rejectsRevokedToken() throws Exception {
    when(invalidatedTokenRepository.existsById("revoked")).thenReturn(true);

    assertInvalid(token(KEY, ISSUER, Instant.now().plusSeconds(300), null, "revoked"));
  }

  @Test
  void propagatesDatabaseFailure() throws Exception {
    when(invalidatedTokenRepository.existsById("id"))
        .thenThrow(new DataAccessResourceFailureException("database unavailable"));
    String token = token(KEY, ISSUER, Instant.now().plusSeconds(300), null, "id");

    assertThatThrownBy(() -> decoder.decode(token))
        .isInstanceOf(DataAccessResourceFailureException.class);
  }

  private void assertInvalid(String token) {
    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  private String token(
      String key, String issuer, Instant expiresAt, Instant notBefore, String jwtId)
      throws Exception {
    Instant now = Instant.now();
    JWTClaimsSet.Builder claims =
        new JWTClaimsSet.Builder().subject("user").issuer(issuer).issueTime(Date.from(now));
    if (expiresAt != null) {
      claims.expirationTime(Date.from(expiresAt));
    }
    if (notBefore != null) {
      claims.notBeforeTime(Date.from(notBefore));
    }
    if (jwtId != null) {
      claims.jwtID(jwtId);
    }
    JWSObject token =
        new JWSObject(
            new JWSHeader(JWSAlgorithm.HS512), new Payload(claims.build().toJSONObject()));
    token.sign(new MACSigner(key.getBytes()));
    return token.serialize();
  }
}
