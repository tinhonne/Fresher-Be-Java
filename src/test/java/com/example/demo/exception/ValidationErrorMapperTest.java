package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.demo.dto.request.customer.CustomerCreateRequest;
import com.example.demo.dto.response.ValidationErrorResponse;
import com.example.demo.exception.mapper.ValidationErrorMapper;
import com.example.demo.validation.ValidationCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

class ValidationErrorMapperTest {

  private final ValidationErrorMapper mapper = new ValidationErrorMapper();
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void mapsAllErrorsWithKnownReasonsAndWhitelistedParams() {
    ValidationFixture target = new ValidationFixture(null, "x");
    List<ObjectError> errors =
        validator.validate(target).stream()
            .map(
                violation -> {
                  String field = violation.getPropertyPath().toString();
                  FieldError error =
                      new FieldError(
                          "target", field, target, false, null, null, violation.getMessage());
                  error.wrap(violation);
                  return (ObjectError) error;
                })
            .toList();

    ValidationErrorResponse result = mapper.map(errors);

    assertEquals(2, result.errors().size());
    ValidationErrorResponse.ValidationError notNull = error(result, "required");
    assertEquals(ValidationCode.NOT_NULL, notNull.reason());
    assertEquals(Map.of(), notNull.params());
    ValidationErrorResponse.ValidationError size = error(result, "sized");
    assertEquals(ValidationCode.SIZE, size.reason());
    assertEquals(2, size.params().get("min"));
    assertEquals(4, size.params().get("max"));
    assertFalse(size.params().containsKey("message"));
    assertFalse(size.params().containsKey("groups"));
    assertFalse(size.params().containsKey("payload"));
  }

  @Test
  void minimumAgeIncludesMinParam() {
    CustomerCreateRequest target = new CustomerCreateRequest();
    target.setBirthday(LocalDate.now().minusYears(10));
    ConstraintViolation<CustomerCreateRequest> violation =
        validator.validate(target).stream()
            .filter(item -> item.getPropertyPath().toString().equals("birthday"))
            .filter(item -> item.getMessage().equals(ValidationCode.MINIMUM_AGE))
            .findFirst()
            .orElseThrow();
    FieldError fieldError =
        new FieldError(
            "target", "birthday", target.getBirthday(), false, null, null, violation.getMessage());
    fieldError.wrap(violation);

    ValidationErrorResponse.ValidationError error =
        mapper.map(List.of(fieldError)).errors().getFirst();

    assertEquals(ValidationCode.MINIMUM_AGE, error.reason());
    assertEquals(18, error.params().get("min"));
  }

  @Test
  void unknownReasonUnwrapFailureAndObjectErrorsUseSafeFallbacks() {
    List<ObjectError> errors =
        List.of(
            new FieldError("target", "unknown", "bad", false, null, null, "UNKNOWN"),
            new FieldError("target", "known", "bad", false, null, null, ValidationCode.SIZE),
            new ObjectError("target", ValidationCode.NOT_NULL));

    ValidationErrorResponse result = mapper.map(errors);

    assertEquals(ValidationCode.INVALID_INPUT, error(result, "unknown").reason());
    assertEquals(ValidationCode.INVALID_INPUT, error(result, "known").reason());
    ValidationErrorResponse.ValidationError objectError = result.errors().get(2);
    assertNull(objectError.field());
    assertEquals(ValidationCode.INVALID_INPUT, objectError.reason());
    assertEquals(Map.of(), objectError.params());
  }

  private ValidationErrorResponse.ValidationError error(
      ValidationErrorResponse result, String field) {
    return result.errors().stream()
        .filter(item -> field.equals(item.field()))
        .findFirst()
        .orElseThrow();
  }

  private record ValidationFixture(
      @NotNull(message = ValidationCode.NOT_NULL) String required,
      @Size(min = 2, max = 4, message = ValidationCode.SIZE) String sized) {}
}
