package com.example.demo.config.properties;

import static com.example.demo.constant.SecurityConstants.MIN_HS512_KEY_BYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rejectsShortAsciiSignerKey() {
    JwtProperties properties = properties("a".repeat(MIN_HS512_KEY_BYTES - 1));

    assertTrue(
        validator.validate(properties).stream()
            .anyMatch(violation -> violation.getMessage().contains("app.jwt.signer-key")));
  }

  @Test
  void validatesSignerKeyByUtf8ByteLength() {
    JwtProperties properties = properties("é".repeat(MIN_HS512_KEY_BYTES / 2));

    assertEquals(MIN_HS512_KEY_BYTES, properties.signerKeyBytes().length);
    assertTrue(validator.validate(properties).isEmpty());
  }

  @Test
  void accepts64ByteSignerKeyAndReturnsDefensiveBytes() {
    JwtProperties properties = properties("a".repeat(MIN_HS512_KEY_BYTES));

    byte[] first = properties.signerKeyBytes();
    byte[] second = properties.signerKeyBytes();
    first[0] = 0;

    assertTrue(validator.validate(properties).isEmpty());
    assertNotSame(first, second);
    assertFalse(first[0] == second[0]);
    assertEquals(properties.signerKey(), new String(second, StandardCharsets.UTF_8));
  }

  private JwtProperties properties(String signerKey) {
    return new JwtProperties(signerKey, "test.vn", Duration.ofHours(1));
  }
}
