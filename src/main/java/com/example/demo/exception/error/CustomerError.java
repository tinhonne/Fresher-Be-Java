package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomerError implements ErrorDefinition {
  CUSTOMER_EXISTED("Khách hàng đã tồn tại", HttpStatus.BAD_REQUEST),
  CUSTOMER_NOT_FOUND("Khách hàng không tồn tại", HttpStatus.NOT_FOUND),
  CUSTOMER_HAS_ACCOUNT(
      "Khách hàng còn tài khoản đang hoạt động, đóng băng hoặc chờ duyệt", HttpStatus.BAD_REQUEST),
  INVALID_CUSTOMER_STATUS("Trạng thái khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
  INVALID_CUSTOMER_STATUS_TRANSITION(
      "Không thể kích hoạt lại khách hàng đã ngừng hoạt động", HttpStatus.BAD_REQUEST),
  INVALID_CUSTOMER_SEARCH("Điều kiện tìm kiếm khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
  INVALID_CUSTOMER_PAGE_REQUEST(
      "Thông tin phân trang khách hàng không hợp lệ", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;
}
