package com.example.demo.validation.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.example.demo.validation.ValidationCode;
import com.example.demo.validation.validator.MinimumAgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {MinimumAgeValidator.class})
public @interface MinimumAge {

  String message() default ValidationCode.MINIMUM_AGE;

  int min();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
