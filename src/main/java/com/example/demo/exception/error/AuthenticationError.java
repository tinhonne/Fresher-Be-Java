package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthenticationError implements ErrorDefinition {
  UNAUTHENTICATED("Đăng nhập không thành công", HttpStatus.UNAUTHORIZED);

  private final String message;
  private final HttpStatus httpStatus;
}
