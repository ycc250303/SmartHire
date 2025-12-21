package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * HR 拒绝候选人请求 DTO
 */
@Data
public class RejectRequest implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 拒绝原因（可选，但建议提供） */
  private String reason;

  /** 是否发送通知（默认 true） */
  private Boolean sendNotification = true;

  /** 可选的通知模板ID */
  private String templateId;
}


