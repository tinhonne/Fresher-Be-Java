package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorDefinition {
  USER_EXISTED("Tài khoản đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("Tài khoản đăng nhập không tồn tại", HttpStatus.NOT_FOUND),
  USER_DISABLED("Tài khoản đã bị khoá", HttpStatus.FORBIDDEN),
  INVALID_USER_UPDATE("Phải có ít nhất một trường cập nhật", HttpStatus.BAD_REQUEST),
  FORBIDDEN_ASSIGN_ROLE("Không được phép gán vai trò bị hạn chế", HttpStatus.FORBIDDEN),
  INCORRECT_OLD_PASSWORD("Mật khẩu cũ không chính xác", HttpStatus.UNAUTHORIZED),
  NEW_PASSWORD_SAME_AS_OLD("Mật khẩu mới phải khác mật khẩu cũ", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;
}
