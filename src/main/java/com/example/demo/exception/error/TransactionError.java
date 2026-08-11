package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransactionError implements ErrorDefinition {
  SOURCE_ACCOUNT_NOT_FOUND("Tài khoản nguồn không tồn tại", HttpStatus.NOT_FOUND),
  SOURCE_ACCOUNT_INACTIVE("Tài khoản nguồn không hoạt động", HttpStatus.BAD_REQUEST),
  DESTINATION_ACCOUNT_NOT_FOUND("Tài khoản đích không tồn tại", HttpStatus.NOT_FOUND),
  DESTINATION_ACCOUNT_INACTIVE("Tài khoản đích không hoạt động", HttpStatus.BAD_REQUEST),
  SAME_ACCOUNT_TRANSFER("Tài khoản nguồn và đích phải khác nhau", HttpStatus.BAD_REQUEST),
  INVALID_TRANSFER_AMOUNT("Số tiền chuyển tối thiểu là 0.01", HttpStatus.BAD_REQUEST),
  INVALID_TRANSACTION_DATE_RANGE("Khoảng thời gian giao dịch không hợp lệ", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;
}
