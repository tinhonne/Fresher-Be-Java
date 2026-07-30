package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeastOneFieldNotNull, Object> {
    private Set<String> fields;

    @Override
    public void initialize(AtLeastOneFieldNotNull annotation) {
        fields = Set.copyOf(Arrays.asList(annotation.fields()));
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return Arrays.stream(value.getClass().getRecordComponents())
                .filter(component -> fields.contains(component.getName()))
                .anyMatch(component -> read(component, value) != null);
    }

    private Object read(RecordComponent component, Object value) {
        try {
            return component.getAccessor().invoke(value);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
