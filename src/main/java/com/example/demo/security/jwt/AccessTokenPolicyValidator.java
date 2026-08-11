package com.example.demo.security.jwt;

import com.example.demo.repository.InvalidatedTokenRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AccessTokenPolicyValidator implements OAuth2TokenValidator<Jwt> {

  private final InvalidatedTokenRepository invalidatedTokenRepository;

  public AccessTokenPolicyValidator(InvalidatedTokenRepository invalidatedTokenRepository) {
    this.invalidatedTokenRepository = invalidatedTokenRepository;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    List<OAuth2Error> errors = new ArrayList<>();
    String jwtId = jwt.getId();

    if (!StringUtils.hasText(jwtId)) {
      errors.add(new OAuth2Error("invalid_token", "The jti claim is required", null));
    }
    if (jwt.getExpiresAt() == null) {
      errors.add(new OAuth2Error("invalid_token", "The exp claim is required", null));
    }
    if (StringUtils.hasText(jwtId) && invalidatedTokenRepository.existsById(jwtId)) {
      errors.add(new OAuth2Error("invalid_token", "The token has been invalidated", null));
    }

    return errors.isEmpty()
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(errors);
  }
}
