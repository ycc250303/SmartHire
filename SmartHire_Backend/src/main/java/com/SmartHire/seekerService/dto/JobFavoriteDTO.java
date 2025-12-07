package com.SmartHire.seekerService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 职位收藏DTO - 用于返回收藏列表
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Data
public class JobFavoriteDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** 收藏ID */
  private Long favoriteId;

  /** 岗位ID */
  private Long jobId;

  /** 收藏时间 */
  private Date createdAt;

  /**
   * 获取收藏时间（返回防御性拷贝）
   */
  public Date getCreatedAt() {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  /**
   * 设置收藏时间（存储防御性拷贝）
   */
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
  }

  /** 岗位名称 */
  private String jobTitle;

  /** 岗位薪资最低 */
  private BigDecimal salaryMin;

  /** 岗位薪资最高 */
  private BigDecimal salaryMax;

  /** 岗位地址 */
  private String address;

  /** 公司名称 */
  private String companyName;

  /** 公司规模：1-0-20 2-20-99 3-100-499 4-500-999 5-1000-3000 6-3000-10000 7-10000以上 */
  private Integer companyScale;

  /** 融资阶段：0-无融资 1-天使轮 2-A轮 3-B轮 4-C轮 5-D轮 6-已上市 */
  private Integer financingStage;

  /** HR名称（真实姓名） */
  private String hrName;

  /** HR头像URL */
  private String hrAvatarUrl;

  /** 学历要求：0-不限 1-专科 2-本科 3-硕士 4-博士 */
  private Integer educationRequired;
}
