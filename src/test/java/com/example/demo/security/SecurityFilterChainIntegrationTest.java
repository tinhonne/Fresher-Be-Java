package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.SecurityConfig;
import com.example.demo.config.properties.JwtProperties;
import com.example.demo.config.properties.SecurityProperties;
import com.example.demo.entity.User;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.authorization.AppRole;
import com.example.demo.security.authorization.RequestAuthorizationManager;
import com.example.demo.service.impl.AuthenticationServiceImpl;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = SecurityFilterChainIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest {

  private static final String KEY =
      "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  private static final String ISSUER = "security-filter-chain-test";

  @Autowired private MockMvc mockMvc;

  @Test
  void anonymousProtectedRequestReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/customers/7")).andExpect(status().isUnauthorized());
  }

  @Test
  void wrongRoleReturnsForbiddenAndCorrectRolePassesAuthorization() throws Exception {
    mockMvc
        .perform(get("/customers/7").header("Authorization", bearer("ROLE_ADMIN")))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/customers/7").header("Authorization", bearer("ROLE_EMPLOYEE")))
        .andExpect(status().isOk());
  }

  @Test
  void publicEndpointAllowsAnonymousOnlyForConfiguredMethod() throws Exception {
    mockMvc.perform(post("/auth/token")).andExpect(status().isOk());
    mockMvc.perform(get("/auth/token")).andExpect(status().isUnauthorized());
  }

  @Test
  void methodsDifferAndPathVariablesMatch() throws Exception {
    mockMvc
        .perform(get("/customers/42").header("Authorization", bearer("ROLE_EMPLOYEE")))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/customers/42").header("Authorization", bearer("ROLE_EMPLOYEE")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(
            get("/accounts/ABC-123/transactions").header("Authorization", bearer("ROLE_EMPLOYEE")))
        .andExpect(status().isOk());
  }

  @Test
  void unknownRoleAndUnmappedProtectedEndpointReturnForbidden() throws Exception {
    mockMvc
        .perform(get("/users/me").header("Authorization", bearer("ROLE_UNKNOWN")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/protected-unmapped").header("Authorization", bearer("ROLE_ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void generatedJwtContainsOnlyRoleAuthoritiesAndNoDatabasePermissionCodes() throws Exception {
    User user = User.builder().username("employee").roles(Set.of(AppRole.EMPLOYEE)).build();
    JwtProperties properties = new JwtProperties(KEY, ISSUER, Duration.ofHours(1));
    AuthenticationServiceImpl service =
        new AuthenticationServiceImpl(
            mock(UserRepository.class),
            mock(PasswordEncoder.class),
            properties,
            mock(InvalidatedTokenRepository.class),
            mock(JwtDecoder.class));

    List<String> scopes =
        List.of(
            SignedJWT.parse(service.generateToken(user))
                .getJWTClaimsSet()
                .getStringClaim("scope")
                .split(" "));

    assertThat(scopes).containsExactly("ROLE_EMPLOYEE");
    assertThat(scopes).allMatch(scope -> scope.startsWith("ROLE_"));
    assertThat(scopes).doesNotContain("CUSTOMER_DELETE_FROM_DB");
  }

  private String bearer(String... authorities) throws Exception {
    Instant now = Instant.now();
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("test-user")
            .issuer(ISSUER)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(300)))
            .jwtID(UUID.randomUUID().toString())
            .claim("scope", String.join(" ", authorities))
            .build();
    JWSObject token =
        new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claims.toJSONObject()));
    token.sign(new MACSigner(KEY.getBytes()));
    return "Bearer " + token.serialize();
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({SecurityConfig.class, RequestAuthorizationManager.class, TestController.class})
  static class TestApplication {

    @Bean
    SecurityProperties securityProperties() {
      return new SecurityProperties(4, null);
    }

    @Bean
    JwtDecoder jwtDecoder() {
      NimbusJwtDecoder decoder =
          NimbusJwtDecoder.withSecretKey(new SecretKeySpec(KEY.getBytes(), "HmacSHA512"))
              .macAlgorithm(MacAlgorithm.HS512)
              .build();
      decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
      return decoder;
    }
  }

  @RestController
  @RequestMapping
  static class TestController {

    @PostMapping("/auth/token")
    ResponseEntity<Void> token() {
      return ResponseEntity.ok().build();
    }

    @GetMapping({
      "/auth/token",
      "/customers/{id}",
      "/accounts/{accountNumber}/transactions",
      "/users/me",
      "/protected-unmapped"
    })
    ResponseEntity<Void> getEndpoint() {
      return ResponseEntity.ok().build();
    }

    @PostMapping("/customers/{id}")
    ResponseEntity<Void> postCustomer() {
      return ResponseEntity.ok().build();
    }
  }
}
