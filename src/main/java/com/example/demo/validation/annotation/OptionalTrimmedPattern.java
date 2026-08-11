package com.example.demo.validation.annotation;

import com.example.demo.validation.ValidationCode;
import com.example.demo.validation.validator.OptionalTrimmedPatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalTrimmedPatternValidator.class)
public @interface OptionalTrimmedPattern {
  String message() default ValidationCode.OPTIONAL_TRIMMED_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String regexp();
}
