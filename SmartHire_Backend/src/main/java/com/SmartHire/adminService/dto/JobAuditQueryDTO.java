package com.SmartHire.adminService.dto;

import lombok.Data;

/**
 * 职位审核查询参数DTO（完全匹配前端需求）
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Data
public class JobAuditQueryDTO {

  /** 审核状态: pending, approved, rejected, modified */
  private String status;

  /** 搜索关键词（职位名称、公司名称、发布者） */
  private String keyword;

  /** 当前页码 */
  private Integer current = 1;

  /** 每页数量 */
  private Integer size = 20;
}
