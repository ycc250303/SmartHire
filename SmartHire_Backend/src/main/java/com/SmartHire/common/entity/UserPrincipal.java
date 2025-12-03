package com.SmartHire.common.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/** 统一的当前登录用户模型，封装从 JWT claims 中解析出来的核心信息 */
@Data
public class UserPrincipal implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 用户 ID */
  private Long id;

  /** 用户名 */
  private String username;

  /** 用户类型，例如：1-求职者，2-HR，3-管理员 */
  private Integer userType;

  /** 原始 claims（如有需要可以拿到其他字段） */
  private Map<String, Object> claims;

  /**
   * 获取 claims 的不可变副本，防止外部修改
   *
   * @return claims 的不可变副本
   */
  public Map<String, Object> getClaims() {
    if (claims == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(new HashMap<>(claims));
  }

  /**
   * 设置 claims，创建防御性拷贝，防止外部修改
   *
   * @param claims 要设置的 claims
   */
  public void setClaims(Map<String, Object> claims) {
    if (claims == null) {
      this.claims = null;
    } else {
      this.claims = new HashMap<>(claims);
    }
  }

  public static UserPrincipal fromClaims(Map<String, Object> claims) {
    if (claims == null) {
      return null;
    }
    UserPrincipal principal = new UserPrincipal();
    Object idObj = claims.get("id");
    if (idObj instanceof Number number) {
      principal.setId(number.longValue());
    } else if (idObj instanceof String s) {
      try {
        principal.setId(Long.parseLong(s));
      } catch (NumberFormatException ignored) {
      }
    }
    Object usernameObj = claims.get("username");
    if (usernameObj != null) {
      principal.setUsername(usernameObj.toString());
    }
    Object userTypeObj = claims.get("userType");
    if (userTypeObj instanceof Number number) {
      principal.setUserType(number.intValue());
    } else if (userTypeObj instanceof String s) {
      try {
        principal.setUserType(Integer.parseInt(s));
      } catch (NumberFormatException ignored) {
      }
    }
    principal.setClaims(claims);
    return principal;
  }
}
