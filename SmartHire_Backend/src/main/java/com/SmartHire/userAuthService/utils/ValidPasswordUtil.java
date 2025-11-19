package com.SmartHire.userAuthService.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义密码验证注解
 * 要求：
 * - 不小于6位
 * - 至少包含数字、小写字母、大写字母的两项
 * - 不能包含特殊字符（只允许字母和数字）
 *
 * @author SmartHire Team
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidatorUtil.class)
@Documented
public @interface ValidPasswordUtil {
    String message() default "密码格式不正确：长度至少6位，至少包含数字、小写字母、大写字母的两项，不能包含特殊字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
