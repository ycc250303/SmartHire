package com.SmartHire.hrService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/** 岗位卡片DTO - 用于求职者端岗位列表展示 */
@Data
public class JobCardDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** 岗位ID */
  private Long jobId;

  /** 岗位名称 */
  private String jobTitle;

  /** 岗位薪资最低 */
  private BigDecimal salaryMin;

  /** 岗位薪资最高 */
  private BigDecimal salaryMax;

  /** 工作城市 */
  private String city;

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

  /** 工作类型：0-全职 1-实习 */
  private Integer jobType;

  /** 学历要求：0-不限 1-专科 2-本科 3-硕士 4-博士 */
  private Integer educationRequired;
}
