package com.SmartHire.adminService.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 职位审核列表展示DTO（完全匹配前端Job接口）
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Data
public class JobAuditListDTO {

  /** 职位ID */
  private Long id;

  /** 职位名称 */
  private String title;

  /** 公司名称 */
  private String company;

  /** 工作地点 */
  private String location;

  /** 薪资范围 */
  private String salary;

  /** 经验要求 */
  private String experience;

  /** 学历要求 */
  private String education;

  /** 职位描述 */
  private String description;

  /** 审核状态: pending, approved, rejected, modified */
  private String status;

  /** 发布者 */
  private String publisher;

  /** 创建时间 */
  private String createTime;

  /** 技能标签 */
  private List<String> tags;

  /** HR用户ID，用于发送通知 */
  private Long hrUserId;

  // 防御性复制方法
  public List<String> getTags() {
    return tags == null ? Collections.emptyList() : new ArrayList<>(tags);
  }

  public void setTags(List<String> tags) {
    this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
  }
}
