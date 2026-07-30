package com.example.demo.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.example.demo.constant.SecurityConstants.MIN_HS512_KEY_BYTES;

/**
 * Configuration properties for signing and validating application JWTs.
 *
 * @param signerKey the secret key used with HS512
 * @param issuer the required JWT issuer
 * @param expiration the lifetime of issued JWTs
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String signerKey,
        @NotBlank String issuer,
        Duration expiration) {
    public JwtProperties {
        issuer = issuer == null ? "test.vn" : issuer;
        expiration = expiration == null ? Duration.ofHours(1) : expiration;
    }

    @AssertTrue(message = "app.jwt.signer-key must contain at least 64 UTF-8 bytes for HS512")
    public boolean isSignerKeyValidForHs512() {
        return signerKey != null && signerKey.getBytes(StandardCharsets.UTF_8).length >= MIN_HS512_KEY_BYTES;
    }

    public byte[] signerKeyBytes() {
        return signerKey.getBytes(StandardCharsets.UTF_8);
    }

    @AssertTrue(message = "must be positive")
    public boolean isExpirationPositive() {
        return expiration != null && !expiration.isNegative() && !expiration.isZero();
    }
}
