package com.SmartHire.common.enums;

public enum UserType {
  /** 求职者 */
  SEEKER(1, "求职者"),

  /** HR */
  HR(2, "HR"),

  /** 管理员 */
  ADMIN(3, "管理员");

  private final Integer code;
  private final String description;

  UserType(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public Integer getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  /** 根据code获取枚举 */
  public static UserType fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (UserType role : values()) {
      if (role.code.equals(code)) {
        return role;
      }
    }
    return null;
  }
}
