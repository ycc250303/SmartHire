package com.SmartHire.adminService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户封禁DTO
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
@Data
public class UserBanDTO {

  /** 封禁原因 */
  @NotBlank(message = "封禁原因不能为空")
  @Size(max = 500, message = "封禁原因不能超过500字符")
  private String banReason;

  /** 封禁时长类型：permanent-永久封禁, temporary-临时封禁 */
  @NotBlank(message = "封禁时长类型不能为空")
  private String banDurationType;

  /** 封禁天数（临时封禁时使用） */
  private Integer banDays;

  /** 是否发送邮件通知 */
  private Boolean sendEmailNotification = false;

  /** 是否发送系统通知 */
  private Boolean sendSystemNotification = true;

  /** 操作管理员ID */
  @NotNull(message = "操作管理员ID不能为空")
  private Long adminId;

  /** 操作管理员用户名 */
  @NotBlank(message = "操作管理员用户名不能为空")
  private String adminUsername;
}
