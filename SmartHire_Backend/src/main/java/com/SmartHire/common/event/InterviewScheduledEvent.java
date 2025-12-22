package com.SmartHire.common.event;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 面试安排事件 DTO
 *
 * @author SmartHire Team
 */
@Data
public class InterviewScheduledEvent implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private Long interviewId;
  private Long applicationId;
  private Long seekerUserId;
  private Long hrUserId;
  private Date interviewTime;
  private String meetingLink;
  private String location;
  private String interviewer;
  private String note;
}


