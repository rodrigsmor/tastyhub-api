package com.rodrigo.tastyhub.shared.infrastructure.aspect;

import com.rodrigo.tastyhub.shared.kernel.annotations.AtLeastOneField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Objects;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return false;

        return Arrays.stream(value.getClass().getRecordComponents())
            .map(rc -> {
                try {
                    return rc.getAccessor().invoke(value);
                } catch (Exception e) {
                    return null;
                }
            })
            .anyMatch(Objects::nonNull);
    }
}
