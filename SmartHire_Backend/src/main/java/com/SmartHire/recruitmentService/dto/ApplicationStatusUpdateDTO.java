package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** 简历状态更新DTO */
@Data
public class ApplicationStatusUpdateDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 状态：0-已投递/已推荐 1-已查看 2-待面试 3-已面试 4-已录用 5-已拒绝 */
  @NotNull(message = "状态不能为空")
  @Min(value = 0, message = "状态值必须在0-6之间")
  @Max(value = 6, message = "状态值必须在0-6之间")
  private Byte status;
}
