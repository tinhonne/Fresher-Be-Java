package com.example.demo.exception;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ValidationErrorResponse;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.mapper.ValidationErrorMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final ValidationErrorMapper validationErrorMapper;

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<?>> handleAppException(AppException exception) {
    return errorResponse(exception.getErrorCode());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationExceptions(
      MethodArgumentNotValidException exception) {
    ValidationErrorResponse result =
        validationErrorMapper.map(exception.getBindingResult().getAllErrors());
    ApiResponse<ValidationErrorResponse> body =
        ApiResponse.<ValidationErrorResponse>builder()
            .code(CommonError.INVALID_INPUT.getCode())
            .message(CommonError.INVALID_INPUT.getMessage())
            .result(result)
            .build();
    return ResponseEntity.status(CommonError.INVALID_INPUT.getHttpStatus()).body(body);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    ConstraintViolationException.class,
    HandlerMethodValidationException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<ApiResponse<?>> handleInvalidInput(Exception exception) {
    return errorResponse(CommonError.INVALID_INPUT);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ApiResponse<?>> handleOptimisticLockingFailure(
      ObjectOptimisticLockingFailureException exception) {
    return errorResponse(CommonError.CONCURRENT_MODIFICATION);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleNoHandlerFound(NoHandlerFoundException exception) {
    return errorResponse(CommonError.RESOURCE_NOT_FOUND);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
      AccessDeniedException exception) {
    return errorResponse(CommonError.FORBIDDEN);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
    log.error("Unhandled exception", exception);
    return errorResponse(CommonError.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ApiResponse<?>> errorResponse(ErrorDefinition error) {
    return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error));
  }
}
