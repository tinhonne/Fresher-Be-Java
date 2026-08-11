package com.example.demo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Security configuration properties.
 *
 * @param passwordStrength the BCrypt log rounds, from 4 through 31
 * @param cors cross-origin resource sharing settings
 */
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(@Min(4) @Max(31) int passwordStrength, @Valid Cors cors) {
  public SecurityProperties {
    passwordStrength = passwordStrength == 0 ? 10 : passwordStrength;
    cors = cors == null ? new Cors(List.of("http://localhost:5173"), Duration.ofHours(1)) : cors;
  }

  /**
   * Cross-origin resource sharing configuration.
   *
   * @param allowedOrigins origins permitted to make cross-origin requests
   * @param maxAge duration browsers may cache a preflight response
   */
  public record Cors(List<String> allowedOrigins, Duration maxAge) {
    public Cors {
      allowedOrigins =
          allowedOrigins == null ? List.of("http://localhost:5173") : List.copyOf(allowedOrigins);
      maxAge = maxAge == null ? Duration.ofHours(1) : maxAge;
    }

    @AssertTrue(message = "must be positive")
    public boolean isMaxAgePositive() {
      return maxAge != null && !maxAge.isNegative() && !maxAge.isZero();
    }
  }
}
