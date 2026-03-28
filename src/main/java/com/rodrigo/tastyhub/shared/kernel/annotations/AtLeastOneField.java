package com.rodrigo.tastyhub.shared.kernel.annotations;

import com.rodrigo.tastyhub.shared.infrastructure.aspect.AtLeastOneFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldValidator.class)
@Documented
public @interface AtLeastOneField {
    String message() default "At least one field must be provided for update";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
