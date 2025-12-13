package com.SmartHire.adminService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新DTO
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
@Data
public class UserStatusUpdateDTO {

  /** 用户ID */
  @NotNull(message = "用户ID不能为空")
  private Long userId;

  /** 目标状态：0-禁用/封禁, 1-正常 */
  @NotNull(message = "目标状态不能为空")
  private Integer targetStatus;

  /** 状态变更原因 */
  @NotBlank(message = "状态变更原因不能为空")
  private String reason;

  /** 操作管理员ID */
  @NotNull(message = "操作管理员ID不能为空")
  private Long adminId;

  /** 操作管理员用户名 */
  @NotBlank(message = "操作管理员用户名不能为空")
  private String adminUsername;

  /** 是否发送通知 */
  private Boolean sendNotification = true;
}
