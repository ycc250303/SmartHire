package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * HR 推荐岗位请求 DTO
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
@Data
public class RecommendRequest implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 岗位ID */
  @NotNull(message = "jobId 不能为空")
  private Long jobId;

  /** 求职者对应的用户ID（前端传 userId，后台换取 jobSeekerId） */
  @NotNull(message = "seekerUserId 不能为空")
  private Long seekerUserId;

  /** 附件简历ID（可选） */
  private Long resumeId;

  /** 推荐备注（可选） */
  private String note;
}


