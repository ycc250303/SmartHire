package com.SmartHire.userAuthService.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 密码验证器 验证规则： 1. 长度至少6位 2. 至少包含数字、小写字母、大写字母的两项 3. 不能包含特殊字符（只允许字母和数字）
 *
 * @author SmartHire Team
 */
public class PasswordValidatorUtil implements ConstraintValidator<ValidPasswordUtil, String> {

  @Override
  public void initialize(ValidPasswordUtil constraintAnnotation) {
    // 初始化方法，可以在这里获取注解参数
  }

  @Override
  public boolean isValid(String password, final ConstraintValidatorContext context) {
    if (password == null || password.isEmpty()) {
      return false;
    }

    // 1. 检查长度：至少6位
    if (password.length() < 6) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("密码长度至少6位").addConstraintViolation();
      return false;
    }

    // 2. 检查是否包含特殊字符（只允许字母和数字）
    if (!password.matches("^[a-zA-Z0-9]+$")) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("密码不能包含特殊字符，只能包含字母和数字").addConstraintViolation();
      return false;
    }

    // 3. 检查是否至少包含数字、小写字母、大写字母的两项
    boolean hasDigit = password.matches(".*\\d.*");
    boolean hasLowerCase = password.matches(".*[a-z].*");
    boolean hasUpperCase = password.matches(".*[A-Z].*");

    int typeCount = 0;
    if (hasDigit) {
      typeCount++;
    }
    if (hasLowerCase) {
      typeCount++;
    }
    if (hasUpperCase) {
      typeCount++;
    }

    if (typeCount < 2) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("密码至少包含数字、小写字母、大写字母的两项")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
