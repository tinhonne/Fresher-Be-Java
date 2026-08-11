package com.example.demo.exception;

public class AppException extends RuntimeException {
  private final ErrorDefinition errorCode;

  public AppException(ErrorDefinition errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public AppException(ErrorDefinition errorCode, String detail) {
    super(errorCode.getMessage() + ": " + detail);
    this.errorCode = errorCode;
  }

  public ErrorDefinition getErrorCode() {
    return errorCode;
  }
}
