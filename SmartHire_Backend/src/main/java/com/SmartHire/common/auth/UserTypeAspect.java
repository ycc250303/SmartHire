package com.SmartHire.common.auth;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
public class UserTypeAspect {
  @Autowired private UserContext userContext;

  /** 拦截所有Controller方法，检查是否需要角色验证 */
  @Before("execution(* com.SmartHire..controller..*(..))")
  public void validateUserRole(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Class<?> targetClass = joinPoint.getTarget().getClass();

    // 优先检查方法上的注解
    RequireUserType methodAnnotation = method.getAnnotation(RequireUserType.class);
    if (methodAnnotation != null) {
      validateRole(methodAnnotation.value());
      return;
    }

    // 检查类上的注解
    RequireUserType classAnnotation = targetClass.getAnnotation(RequireUserType.class);
    if (classAnnotation != null) {
      validateRole(classAnnotation.value());
    }
  }

  /**
   * 验证用户是否具有指定角色之一
   *
   * @param requiredTypes 允许的角色列表（OR关系）
   */
  private void validateRole(UserType[] requiredTypes) {
    if (requiredTypes == null || requiredTypes.length == 0) {
      // 如果没有指定角色，则不进行验证
      return;
    }

    Integer userType = userContext.getCurrentUserType();

    if (userType == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 将允许的角色代码转换为Set
    Set<Integer> allowedCodes =
        Arrays.stream(requiredTypes).map(UserType::getCode).collect(Collectors.toSet());

    // 检查用户角色是否在允许的角色列表中
    if (!allowedCodes.contains(userType)) {
      // 根据第一个要求的角色确定错误类型（用于更精确的错误提示）
      UserType firstType = requiredTypes[0];
      throw createTypeException(firstType);
    }
  }

  /** 根据角色创建对应的异常 */
  private BusinessException createTypeException(UserType type) {
    return switch (type) {
      case SEEKER -> new BusinessException(ErrorCode.USER_NOT_SEEKER);
      case HR -> new BusinessException(ErrorCode.USER_NOT_HR);
      case ADMIN -> new BusinessException(ErrorCode.PERMISSION_DENIED);
    };
  }
}
