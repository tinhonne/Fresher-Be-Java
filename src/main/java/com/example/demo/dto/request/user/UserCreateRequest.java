package com.example.demo.dto.request.user;

import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.security.authorization.AppRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserCreateRequest(
    @NotBlank @Size(max = USERNAME_MAX_LENGTH) String username,
    @NotBlank @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH) String password,
    @NotBlank @Size(max = USER_NAME_MAX_LENGTH) String name,
    Set<@NotNull AppRole> roles) {
  /**
   * Creates a user request and normalizes missing role identifiers to an empty set.
   *
   * @param username the username
   * @param password the password
   * @param name the user's name
   * @param roles the role identifiers
   */
  public UserCreateRequest {
    if (roles == null) {
      roles = Set.of();
    }
  }
}
