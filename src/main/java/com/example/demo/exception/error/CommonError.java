package com.example.demo.exception.error;

import com.example.demo.exception.ErrorDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonError implements ErrorDefinition {
  SUCCESS("Thành công", HttpStatus.OK),
  INVALID_INPUT("Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
  FORBIDDEN("Không có quyền truy cập", HttpStatus.FORBIDDEN),
  RESOURCE_NOT_FOUND("Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
  CONCURRENT_MODIFICATION("Dữ liệu đã được thay đổi bởi yêu cầu khác", HttpStatus.CONFLICT),
  INVALID_PAGE_REQUEST("Thông tin phân trang không hợp lệ", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String message;
  private final HttpStatus httpStatus;
}
