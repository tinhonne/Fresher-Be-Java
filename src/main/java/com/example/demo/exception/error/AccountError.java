package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AccountError implements ErrorDefinition {
  ACCOUNT_NOT_FOUND("Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
  ACCOUNT_NUMBER_EXISTED("Số tài khoản đã tồn tại", HttpStatus.CONFLICT),
  ACCOUNT_NOT_PENDING_APPROVAL(
      "Tài khoản không ở trạng thái chờ phê duyệt", HttpStatus.BAD_REQUEST),
  INVALID_ACCOUNT_STATUS("Trạng thái tài khoản không hợp lệ", HttpStatus.BAD_REQUEST),
  CUSTOMER_INACTIVE("Khách hàng không hoạt động", HttpStatus.BAD_REQUEST),
  ACCOUNT_BALANCE_NOT_ZERO("Số dư tài khoản phải bằng không", HttpStatus.BAD_REQUEST),
  INVALID_INITIAL_BALANCE("Số dư ban đầu phải bằng không", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;
}
