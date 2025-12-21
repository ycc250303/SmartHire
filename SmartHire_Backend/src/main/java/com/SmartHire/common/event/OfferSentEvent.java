package com.SmartHire.common.event;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Offer 发送事件 DTO（最小可行）
 */
@Data
public class OfferSentEvent implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Long applicationId;
  private Long seekerUserId;
  private Long hrUserId;
  private String title;
  private Double baseSalary;
  private Double bonus;
  private Date startDate;
  private String employmentType;
  private String note;
}


