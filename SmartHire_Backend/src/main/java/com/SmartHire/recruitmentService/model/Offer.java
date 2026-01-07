package com.SmartHire.recruitmentService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * Offer表（录用通知书）
 *
 * @author SmartHire Team
 * @since 2026-01-07
 */
@Data
@TableName("offer")
public class Offer implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Offer ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 投递记录ID */
  private Long applicationId;

  /** 求职者ID */
  private Long jobSeekerId;

  /** HR ID */
  private Long hrId;

  /** 基本薪资 */
  private BigDecimal baseSalary;

  /** 到岗日期 */
  private Date startDate;

  /** Offer状态：0-待接受 1-已接受 2-已拒绝 */
  private Byte status;

  /** 接受时间 */
  private Date acceptedAt;

  /** 拒绝时间 */
  private Date rejectedAt;

  /** 创建时间 */
  private Date createdAt;

  /** 更新时间 */
  private Date updatedAt;
}

