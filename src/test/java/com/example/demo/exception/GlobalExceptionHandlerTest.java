package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ValidationErrorResponse;
import com.example.demo.entity.Customer;
import com.example.demo.exception.error.CommonError;
import com.example.demo.exception.mapper.ValidationErrorMapper;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler =
      new GlobalExceptionHandler(new ValidationErrorMapper());

  @Test
  void optimisticLockingFailureMapsToConcurrentModification() {
    ObjectOptimisticLockingFailureException exception =
        new ObjectOptimisticLockingFailureException(Customer.class, 5L);

    assertResponse(
        CommonError.CONCURRENT_MODIFICATION, handler.handleOptimisticLockingFailure(exception));
  }

  @Test
  void validationMapsToInvalidInputWithStructuredResult() throws Exception {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
    bindingResult.addError(new FieldError("target", "field", "INVALID"));
    Method method =
        GlobalExceptionHandlerTest.class.getDeclaredMethod("validationArgument", Object.class);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(
            new org.springframework.core.MethodParameter(method, 0), bindingResult);

    ResponseEntity<ApiResponse<?>> response = handler.handleValidationExceptions(exception);

    assertEquals(CommonError.INVALID_INPUT.getHttpStatus(), response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(CommonError.INVALID_INPUT.getCode(), response.getBody().getCode());
    assertEquals(CommonError.INVALID_INPUT.getMessage(), response.getBody().getMessage());
    ValidationErrorResponse result = (ValidationErrorResponse) response.getBody().getResult();
    assertEquals(1, result.errors().size());
    assertEquals("field", result.errors().getFirst().field());
  }

  @Test
  void unexpectedExceptionMapsToInternalServerError() {
    assertResponse(
        CommonError.INTERNAL_SERVER_ERROR,
        handler.handleException(new RuntimeException("write failed")));
  }

  private void validationArgument(Object value) {}

  private void assertResponse(ErrorDefinition expected, ResponseEntity<ApiResponse<?>> response) {
    assertEquals(expected.getHttpStatus(), response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expected.getCode(), response.getBody().getCode());
    assertEquals(expected.getMessage(), response.getBody().getMessage());
  }
}
