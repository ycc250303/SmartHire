package com.SmartHire.common.auth;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.JwtUtil;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/** 用户上下文工具类 统一提供从SecurityContext中获取当前登录用户信息的方法 */
@Component
@Slf4j
public class UserContext {

  @Autowired
  private JwtUtil jwtUtil;

  /**
   * 获取当前登录用户的完整信息
   *
   * @return UserInfo对象，包含用户ID、用户名、用户类型
   * @throws BusinessException 如果用户未登录或信息不完整
   */
  public UserInfo getCurrentUser() {
    Map<String, Object> claims = getCurrentClaims();
    return parseUserInfo(claims);
  }

  /**
   * 获取当前登录用户的ID
   *
   * @return 用户ID
   * @throws BusinessException 如果用户未登录或ID不存在
   */
  public Long getCurrentUserId() {
    UserInfo userInfo = getCurrentUser();
    if (userInfo.getId() == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    return userInfo.getId();
  }

  /**
   * 获取当前登录用户的用户名
   *
   * @return 用户名
   * @throws BusinessException 如果用户未登录或用户名不存在
   */
  public String getCurrentUsername() {
    UserInfo userInfo = getCurrentUser();
    if (userInfo.getUsername() == null || userInfo.getUsername().isBlank()) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }
    return userInfo.getUsername();
  }

  /**
   * 获取当前登录用户的用户类型
   *
   * @return 用户类型：1-求职者，2-HR，3-管理员
   * @throws BusinessException 如果用户未登录或用户类型不存在
   */
  public Integer getCurrentUserType() {
    try {
      UserInfo userInfo = getCurrentUser();
      if (userInfo.getUserType() == null) {
        log.error("用户类型为空: userId={}, username={}", userInfo.getId(), userInfo.getUsername());
        throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
      }
      log.info("成功获取用户类型: userId={}, userType={}", userInfo.getId(), userInfo.getUserType());
      return userInfo.getUserType();
    } catch (BusinessException e) {
      log.error("获取用户类型失败: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * 检查当前用户是否为求职者
   *
   * @return true如果是求职者，否则false
   */
  public boolean isSeeker() {
    try {
      return getCurrentUserType() == 1;
    } catch (BusinessException e) {
      return false;
    }
  }

  /**
   * 检查当前用户是否为HR
   *
   * @return true如果是HR，否则false
   */
  public boolean isHr() {
    try {
      return getCurrentUserType() == 2;
    } catch (BusinessException e) {
      return false;
    }
  }

  /**
   * 检查当前用户是否为管理员
   *
   * @return true如果是管理员，否则false
   */
  public boolean isAdmin() {
    try {
      return getCurrentUserType() == 3;
    } catch (BusinessException e) {
      return false;
    }
  }

  /**
   * 从SecurityContext中获取Claims
   *
   * @return Claims Map
   * @throws BusinessException 如果用户未登录
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> getCurrentClaims() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }

    throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
  }

  /**
   * 从Claims中解析用户信息
   *
   * @param claims JWT Claims
   * @return UserInfo对象
   * @throws BusinessException 如果Claims格式不正确
   */
  private UserInfo parseUserInfo(Map<String, Object> claims) {
    if (claims == null || claims.isEmpty()) {
      throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }

    Long userId = jwtUtil.getUserIdFromToken(claims);
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    String username = (String) claims.get("username");
    Object userTypeObj = claims.get("userType");

    Integer userType = null;
    if (userTypeObj instanceof Integer) {
      userType = (Integer) userTypeObj;
    } else if (userTypeObj instanceof Number) {
      userType = ((Number) userTypeObj).intValue();
    }

    log.info("解析用户信息: userId={}, username={}, userType={}", userId, username, userType);
    return new UserInfo(userId, username, userType);
  }
}
