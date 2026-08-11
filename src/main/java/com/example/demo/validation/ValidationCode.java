package com.example.demo.validation;

import java.util.Set;

public final class ValidationCode {

  public static final String NOT_NULL = "NOT_NULL";
  public static final String NOT_BLANK = "NOT_BLANK";
  public static final String NOT_EMPTY = "NOT_EMPTY";
  public static final String SIZE = "SIZE";
  public static final String PATTERN = "PATTERN";
  public static final String MIN = "MIN";
  public static final String MAX = "MAX";
  public static final String POSITIVE = "POSITIVE";
  public static final String POSITIVE_OR_ZERO = "POSITIVE_OR_ZERO";
  public static final String DECIMAL_MIN = "DECIMAL_MIN";
  public static final String DIGITS = "DIGITS";
  public static final String PAST_OR_PRESENT = "PAST_OR_PRESENT";
  public static final String MINIMUM_AGE = "MINIMUM_AGE";
  public static final String AT_LEAST_ONE_FIELD_NOT_NULL = "AT_LEAST_ONE_FIELD_NOT_NULL";
  public static final String OPTIONAL_TRIMMED_PATTERN = "OPTIONAL_TRIMMED_PATTERN";
  public static final String INVALID_INPUT = "INVALID_INPUT";

  private static final Set<String> VALUES =
      Set.of(
          NOT_NULL,
          NOT_BLANK,
          NOT_EMPTY,
          SIZE,
          PATTERN,
          MIN,
          MAX,
          POSITIVE,
          POSITIVE_OR_ZERO,
          DECIMAL_MIN,
          DIGITS,
          PAST_OR_PRESENT,
          MINIMUM_AGE,
          AT_LEAST_ONE_FIELD_NOT_NULL,
          OPTIONAL_TRIMMED_PATTERN,
          INVALID_INPUT);

  private ValidationCode() {}

  public static boolean isDefined(String code) {
    return VALUES.contains(code);
  }
}
