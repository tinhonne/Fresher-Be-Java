package com.example.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * Defines the common contract for application error types.
 *
 * <p>Implementations provide the HTTP status and message associated with an error. The enum
 * constant name is used as the error code returned to the client.
 *
 * <p>This interface is intended to be implemented by error-code enums.
 */
public interface ErrorDefinition {

  String getMessage();

  HttpStatus getHttpStatus();

  /** Uses the enum constant name as the public error code. */
  default String getCode() {
    return ((Enum<?>) this).name();
  }
}
