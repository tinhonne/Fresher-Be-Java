package com.example.demo.config;

import com.example.demo.config.properties.JwtProperties;
import com.example.demo.security.jwt.AccessTokenPolicyValidator;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

  @Bean
  JwtDecoder jwtDecoder(
      JwtProperties jwtProperties, AccessTokenPolicyValidator accessTokenPolicyValidator) {
    SecretKeySpec secretKey =
        new SecretKeySpec(jwtProperties.signerKeyBytes(), MacAlgorithm.HS512.getName());
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS512).build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<Jwt>(
            JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()),
            accessTokenPolicyValidator));
    return decoder;
  }
}
