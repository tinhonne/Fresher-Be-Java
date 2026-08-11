package com.example.demo.validation.validator;

import com.example.demo.validation.annotation.MinimumAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

  private int min;

  @Override
  public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
    if (Objects.isNull(value)) return true;

    long year = ChronoUnit.YEARS.between(value, LocalDate.now());
    return year >= min;
  }

  @Override
  public void initialize(MinimumAge constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    min = constraintAnnotation.min();
  }
}
