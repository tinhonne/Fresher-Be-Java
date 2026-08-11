package com.example.demo.dto.response;

import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(List<ValidationError> errors) {

  public record ValidationError(String field, String reason, Map<String, Object> params) {}
}
