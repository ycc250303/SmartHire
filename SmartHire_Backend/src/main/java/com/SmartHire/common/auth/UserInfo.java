package com.SmartHire.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户信息实体类 从JWT中解析出的用户基本信息 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
  /** 用户ID */
  private Long id;

  /** 用户名 */
  private String username;

  /** 用户类型：1-求职者，2-HR，3-管理员 */
  private Integer userType;
}
