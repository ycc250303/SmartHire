package com.SmartHire.common.event;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 投递被拒事件 DTO
 */
@Data
public class ApplicationRejectedEvent implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Long applicationId;
  private Long seekerUserId;
  private Long hrUserId;
  private String reason;
  private String templateId;
}


