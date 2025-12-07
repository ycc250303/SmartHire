package com.SmartHire.shared.aspect;

import com.SmartHire.shared.annotation.RequireAdmin;
import com.SmartHire.shared.annotation.RequireHr;
import com.SmartHire.shared.annotation.RequireRole;
import com.SmartHire.shared.annotation.RequireSeeker;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.SecurityContextUtil;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 角色授权切面
 * 统一处理用户身份验证和授权检查
 * 
 * @author SmartHire Team
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在事务切面之前执行
public class RoleAuthorizationAspect {

  @Autowired
  private JwtUtil jwtUtil;

  /**
   * 处理 @RequireRole 注解
   */
  @Before("@annotation(com.SmartHire.shared.annotation.RequireRole) || @within(com.SmartHire.shared.annotation.RequireRole)")
  public void checkRequireRole(JoinPoint joinPoint) {
    RequireRole requireRole = getAnnotation(joinPoint, RequireRole.class);
    if (requireRole != null) {
      int[] allowedUserTypes = requireRole.value();
      validateUserType(allowedUserTypes);
    }
  }

  /**
   * 处理 @RequireHr 注解
   */
  @Before("@annotation(com.SmartHire.shared.annotation.RequireHr) || @within(com.SmartHire.shared.annotation.RequireHr)")
  public void checkRequireHr(JoinPoint joinPoint) {
    validateUserType(new int[]{2}); // 2 = HR
  }

  /**
   * 处理 @RequireSeeker 注解
   */
  @Before("@annotation(com.SmartHire.shared.annotation.RequireSeeker) || @within(com.SmartHire.shared.annotation.RequireSeeker)")
  public void checkRequireSeeker(JoinPoint joinPoint) {
    validateUserType(new int[]{1}); // 1 = 求职者
  }

  /**
   * 处理 @RequireAdmin 注解
   */
  @Before("@annotation(com.SmartHire.shared.annotation.RequireAdmin) || @within(com.SmartHire.shared.annotation.RequireAdmin)")
  public void checkRequireAdmin(JoinPoint joinPoint) {
    validateUserType(new int[]{3}); // 3 = 管理员
  }

  /**
   * 验证用户类型
   *
   * @param allowedUserTypes 允许的用户类型数组
   * @throws BusinessException 如果用户未登录或类型不匹配
   */
  private void validateUserType(int[] allowedUserTypes) {
    // 获取当前用户的Claims
    Map<String, Object> claims = SecurityContextUtil.getCurrentClaims();
    if (claims.isEmpty()) {
      log.warn("用户未登录，无法进行身份验证");
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }

    // 获取用户类型
    Integer userType = jwtUtil.getUserTypeFromToken(claims);
    if (userType == null) {
      log.warn("JWT token中缺少userType信息");
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }

    // 检查用户类型是否在允许的列表中
    boolean isAllowed = Arrays.stream(allowedUserTypes)
        .anyMatch(type -> type == userType);

    if (!isAllowed) {
      log.warn("用户类型不匹配，当前用户类型: {}, 允许的类型: {}", userType, Arrays.toString(allowedUserTypes));
      // 根据用户类型返回相应的错误信息
      if (userType == 1) {
        throw new BusinessException(ErrorCode.USER_NOT_HR);
      } else if (userType == 2) {
        throw new BusinessException(ErrorCode.USER_NOT_SEEKER);
      } else {
        throw new BusinessException(ErrorCode.PERMISSION_DENIED);
      }
    }

    log.debug("用户身份验证通过，用户类型: {}", userType);
  }

  /**
   * 从方法或类上获取注解
   */
  private <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    T annotation = signature.getMethod().getAnnotation(annotationClass);
    if (annotation != null) {
      return annotation;
    }
    // 如果方法上没有，尝试从类上获取
    return joinPoint.getTarget().getClass().getAnnotation(annotationClass);
  }
}

