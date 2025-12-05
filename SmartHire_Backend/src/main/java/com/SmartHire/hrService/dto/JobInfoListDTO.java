package com.SmartHire.hrService.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

/** 岗位列表DTO */
@Data
public class JobInfoListDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 职位ID */
  private Long id;

  /** 职位名称 */
  private String jobTitle;

  /** 职位类别 */
  private String jobCategory;

  /** 部门 */
  private String department;

  /** 工作城市 */
  private String city;

  /** 薪资最低 */
  private BigDecimal salaryMin;

  /** 薪资最高 */
  private BigDecimal salaryMax;

  /** 薪资月数 */
  private Integer salaryMonths;

  /** 学历要求 */
  private Integer educationRequired;

  /** 工作类型 */
  private Integer jobType;

  /** 经验要求（仅全职类型需要）0-应届生 1-1年以内 2-1-3年 3-3-5年 4-5-10年 5-10年以上 */
  private Integer experienceRequired;

  /** 每周实习天数（仅实习类型需要） */
  private Integer internshipDaysPerWeek;

  /** 实习时长（月为单位，仅实习类型需要） */
  private Integer internshipDurationMonths;

  /** 状态：0-已下线 1-招聘中 2-已暂停 */
  private Integer status;

  /** 浏览次数 */
  private Integer viewCount;

  /** 申请次数 */
  private Integer applicationCount;

  /** 发布时间 */
  private Date publishedAt;

  /** 创建时间 */
  private Date createdAt;

  /** 更新时间 */
  private Date updatedAt;

  /** 技能要求列表 */
  private List<String> skills;
}
