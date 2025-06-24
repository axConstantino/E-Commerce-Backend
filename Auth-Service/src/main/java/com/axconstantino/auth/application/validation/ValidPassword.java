package com.axconstantino.auth.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must be 6-20 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
