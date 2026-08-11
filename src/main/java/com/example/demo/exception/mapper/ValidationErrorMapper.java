package com.example.demo.exception.mapper;

import com.example.demo.dto.response.ValidationErrorResponse;
import com.example.demo.validation.ValidationCode;
import jakarta.validation.ConstraintViolation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

@Component
public class ValidationErrorMapper {

  private static final Set<String> PARAMETER_NAMES =
      Set.of("min", "max", "value", "integer", "fraction");

  public ValidationErrorResponse map(List<ObjectError> errors) {
    return new ValidationErrorResponse(errors.stream().map(this::map).toList());
  }

  ValidationErrorResponse.ValidationError map(ObjectError error) {
    String field = error instanceof FieldError fieldError ? fieldError.getField() : null;
    String reason = ValidationCode.INVALID_INPUT;
    Map<String, Object> params = Map.of();
    try {
      String defaultMessage = error.getDefaultMessage();
      if (ValidationCode.isDefined(defaultMessage)) {
        ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
        reason = defaultMessage;
        params = annotationParams(violation.getConstraintDescriptor().getAnnotation());
      }
    } catch (Exception ignored) {
      reason = ValidationCode.INVALID_INPUT;
      params = Map.of();
    }
    return new ValidationErrorResponse.ValidationError(field, reason, params);
  }

  private Map<String, Object> annotationParams(Annotation annotation) {
    Map<String, Object> params = new LinkedHashMap<>();
    for (Method method : annotation.annotationType().getDeclaredMethods()) {
      if (PARAMETER_NAMES.contains(method.getName())) {
        try {
          params.put(method.getName(), method.invoke(annotation));
        } catch (Exception ignored) {
          return Map.of();
        }
      }
    }
    return params;
  }
}
