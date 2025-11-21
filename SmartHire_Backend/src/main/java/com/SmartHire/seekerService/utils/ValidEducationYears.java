package com.SmartHire.seekerService.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EducationYearsValidator.class)
public @interface ValidEducationYears {
    String message() default "教育年份不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
