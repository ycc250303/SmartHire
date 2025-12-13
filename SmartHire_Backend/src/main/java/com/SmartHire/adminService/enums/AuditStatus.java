package com.SmartHire.adminService.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Getter
@AllArgsConstructor
public enum AuditStatus {

  /** 草稿状态 */
  DRAFT("draft", "草稿"),

  /** 待审核 */
  PENDING("pending", "待审核"),

  /** 已通过 */
  APPROVED("approved", "已通过"),

  /** 已拒绝 */
  REJECTED("rejected", "已拒绝"),

  /** 需修改 */
  MODIFIED("modified", "需修改");

  /** 状态码 */
  private final String code;

  /** 状态描述 */
  private final String description;

  /**
   * 根据状态码获取枚举
   *
   * @param code 状态码
   * @return 对应的枚举，如果找不到返回DRAFT
   */
  public static AuditStatus fromCode(String code) {
    if (code == null) {
      return DRAFT;
    }

    for (AuditStatus status : values()) {
      if (status.getCode().equals(code)) {
        return status;
      }
    }
    return DRAFT;
  }

  /**
   * 判断是否为可审核状态
   *
   * @return true如果可以审核
   */
  public boolean isAuditable() {
    return this == PENDING;
  }

  /**
   * 判断是否为终态
   *
   * @return true如果为终态
   */
  public boolean isFinal() {
    return this == APPROVED || this == REJECTED;
  }
}
