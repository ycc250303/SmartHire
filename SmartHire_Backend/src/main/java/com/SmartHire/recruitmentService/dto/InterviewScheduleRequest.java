package com.SmartHire.recruitmentService.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 面试安排请求 DTO（HR -> 求职者）
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
@Data
public class InterviewScheduleRequest implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 关联的投递记录ID */
  @NotNull(message = "applicationId 不能为空")
  private Long applicationId;

  /** 面试时间 */
  @NotNull(message = "interviewTime 不能为空")
  private Date interviewTime;

  /** 持续时长（分钟） */
  private Integer duration;

  /** 面试类型：1-电话 2-视频 3-现场 */
  private Integer interviewType;

  /** 面试轮次 */
  private Integer interviewRound;

  /** 面试地点或会议室 */
  private String location;

  /** 线上会议链接（video） */
  private String meetingLink;

  /** 面试官（字符串描述或逗号分隔id） */
  private String interviewer;

  /** 是否立即通知候选人（true = 发送通知） */
  private Boolean notifyCandidate = true;

  /** 通知内容（可选，若为空则使用默认模板） */
  private String note;
}


