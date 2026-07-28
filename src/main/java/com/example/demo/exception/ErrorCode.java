package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ===== Common (1xxx) =====
    SUCCESS(1000, "Thành công", HttpStatus.OK),
    INVALID_INPUT(1001, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1002, "Không có quyền truy cập", HttpStatus.FORBIDDEN),

    // ===== Customer (2xxx) =====
    CUSTOMER_EXISTED(2001, "Khách hàng đã tồn tại", HttpStatus.BAD_REQUEST),
    CUSTOMER_NOT_FOUND(2002, "Khách hàng không tồn tại", HttpStatus.NOT_FOUND),
    CUSTOMER_HAS_ACCOUNT(2003, "Khách hàng còn tài khoản đang hoạt động, không thể xóa", HttpStatus.BAD_REQUEST),

    // ===== Account (3xxx) =====
    ACCOUNT_NOT_FOUND(3001, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    ACCOUNT_NUMBER_EXISTED(3002, "Số tài khoản đã tồn tại", HttpStatus.CONFLICT),
    ACCOUNT_NOT_PENDING_APPROVAL(3003, "Tài khoản không ở trạng thái chờ phê duyệt", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_STATUS(3004, "Trạng thái tài khoản không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== User / Auth (4xxx - 5xxx) =====
    USER_EXISTED(4001, "Tài khoản đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(4002, "Tài khoản đăng nhập không tồn tại", HttpStatus.NOT_FOUND),
    USER_DISABLED(4003, "Tài khoản đã bị khoá", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(5001, "Đăng nhập không thành công", HttpStatus.UNAUTHORIZED),

    // ===== Transfer (6xxx) =====
    SOURCE_ACCOUNT_NOT_FOUND(6001, "Tài khoản nguồn không tồn tại", HttpStatus.NOT_FOUND),
    SOURCE_ACCOUNT_INACTIVE(6002, "Tài khoản nguồn không hoạt động", HttpStatus.BAD_REQUEST),
    DESTINATION_ACCOUNT_NOT_FOUND(6003, "Tài khoản đích không tồn tại", HttpStatus.NOT_FOUND),
    DESTINATION_ACCOUNT_INACTIVE(6004, "Tài khoản đích không hoạt động", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER(6005, "Tài khoản nguồn và đích phải khác nhau", HttpStatus.BAD_REQUEST),
    INVALID_TRANSFER_AMOUNT(6006, "Số tiền chuyển tối thiểu là 0.01", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_DATE_RANGE(6007, "Khoảng thời gian giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_REQUEST(6008, "Thông tin phân trang không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== Permission (7xxx) =====
    PERMISSION_EXISTED(7001, "Permission đã tồn tại", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(7002, "Permission không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PERMISSION_NAME(7003, "Tên permission không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== Role (8xxx) =====
    ROLE_EXISTED(8001, "Role đã tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(8002, "Role không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_HAS_USER(8003, "Role đang được gán cho người dùng, không thể xóa", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_NAME(8004, "Tên role không hợp lệ", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSION_NOT_FOUND(8005, "Permission gán cho role không tồn tại", HttpStatus.NOT_FOUND),
    DEFAULT_ROLE_CANNOT_BE_DELETED(8006, "Không thể xóa role mặc định của hệ thống", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ASSIGN_ROLE(8007, "Không có quyền gán role này", HttpStatus.FORBIDDEN),
    // ===== System (9xxx) =====
    INTERNAL_SERVER_ERROR(9999, "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}