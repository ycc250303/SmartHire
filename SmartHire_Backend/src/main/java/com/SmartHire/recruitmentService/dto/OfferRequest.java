package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * HR 发 Offer 请求 DTO（最小可行，不建表版本）
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
@Data
public class OfferRequest implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 关联的投递记录ID */
  @NotNull(message = "applicationId 不能为空")
  private Long applicationId;

  /** 岗位名称/职位（可选） */
  private String title;

  /** 基本薪资 */
  private Double baseSalary;

  /** 奖金/补贴 */
  private Double bonus;

  /** 到岗日期 */
  private Date startDate;

  /** 雇佣类型（full_time/part_time/intern） */
  private String employmentType;

  /** 是否立即发送（true=发送并将 application.status 置为已录用） */
  private Boolean send = true;

  /** Offer 文本或备注（会作为通知内容的一部分） */
  private String note;
}


