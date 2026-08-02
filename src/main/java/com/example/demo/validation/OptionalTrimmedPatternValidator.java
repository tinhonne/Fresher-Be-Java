package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class OptionalTrimmedPatternValidator implements ConstraintValidator<OptionalTrimmedPattern, String> {
    private Pattern pattern;

    @Override
    public void initialize(OptionalTrimmedPattern annotation) {
        pattern = Pattern.compile(annotation.regexp());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || pattern.matcher(value.trim()).matches();
    }
}
