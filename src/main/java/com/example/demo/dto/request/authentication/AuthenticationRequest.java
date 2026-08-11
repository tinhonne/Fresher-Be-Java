package com.example.demo.dto.request.authentication;

import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.validation.ValidationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(max = USERNAME_MAX_LENGTH, message = ValidationCode.SIZE)
  private String username;

  @NotBlank(message = ValidationCode.NOT_BLANK)
  @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = ValidationCode.SIZE)
  private String password;
}
