package com.SmartHire.shared.utils;

import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 从 Spring Security 上下文中获取当前请求的认证信息 */
public final class SecurityContextUtil {

  private SecurityContextUtil() {}

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getCurrentClaims() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return Collections.emptyMap();
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }
    return Collections.emptyMap();
  }

  /**
   * 获取当前登录用户的ID
   * 注意：需要配合JwtUtil使用
   *
   * @param jwtUtil JwtUtil实例
   * @return 用户ID
   * @throws BusinessException 如果用户未登录或ID不存在
   */
  public static Long getCurrentUserId(JwtUtil jwtUtil) {
    Map<String, Object> claims = getCurrentClaims();
    if (claims.isEmpty()) {
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }
    Long userId = jwtUtil.getUserIdFromToken(claims);
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    return userId;
  }

  /**
   * 获取当前登录用户的类型
   * 注意：需要配合JwtUtil使用
   *
   * @param jwtUtil JwtUtil实例
   * @return 用户类型，1=求职者, 2=HR, 3=管理员
   * @throws BusinessException 如果用户未登录或类型不存在
   */
  public static Integer getCurrentUserType(JwtUtil jwtUtil) {
    Map<String, Object> claims = getCurrentClaims();
    if (claims.isEmpty()) {
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }
    Integer userType = jwtUtil.getUserTypeFromToken(claims);
    if (userType == null) {
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }
    return userType;
  }

  /**
   * 检查当前用户是否为指定类型
   * 注意：需要配合JwtUtil使用
   *
   * @param jwtUtil JwtUtil实例
   * @param expectedUserType 期望的用户类型
   * @return true 如果是指定类型
   */
  public static boolean isUserType(JwtUtil jwtUtil, Integer expectedUserType) {
    try {
      Integer currentUserType = getCurrentUserType(jwtUtil);
      return expectedUserType.equals(currentUserType);
    } catch (BusinessException e) {
      return false;
    }
  }
}
