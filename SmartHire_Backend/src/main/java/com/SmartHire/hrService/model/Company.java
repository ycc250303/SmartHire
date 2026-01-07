package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公司信息实体类 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("company")
public class Company implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 公司ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 公司老板的用户ID */
  private Long ownerUserId;

  /** 公司名称 */
  private String companyName;

  /** 公司简介 */
  private String description;

  /**
   * 公司规模：1-0-20 2-20-99 3-100-499 4-500-999 5-1000-3000 6-3000-10000 7-10000以上
   */
  private Integer companyScale;

  /** 融资阶段：0-无融资 1-天使轮 2-A轮 3-B轮 4-C轮 5-D轮 6-已上市 */
  private Integer financingStage;

  /** 公司行业 */
  private String industry;

  /** 公司网站 */
  private String website;

  /** 公司Logo */
  private String logoUrl;

  /** 福利待遇 */
  private String benefits;

  /** 状态：0-未认证 1-已认证 */
  private Integer status;

  /** 创建时间 */
  private Date createdAt;

  /** 更新时间 */
  private Date updatedAt;

  /** 公司成立时间 */
  private Date companyCreatedAt;

  /** 注册资本 */
  private Integer registeredCapital;

  /** 审核状态 */
  private String auditStatus;

  /** 审核时间 */
  private Date auditedAt;
}