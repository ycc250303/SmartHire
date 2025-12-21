package com.SmartHire.recruitmentService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * interview 表实体
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
@Data
@TableName("interview")
public class Interview implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private Long applicationId;

  /** 面试类型：1-电话 2-视频 3-现场 */
  private Byte interviewType;

  /** 面试轮次 */
  private Integer interviewRound;

  /** 面试时间 */
  private Date interviewTime;

  /** 时长（分钟） */
  private Integer duration;

  /** 地点 */
  private String location;

  /** 会议链接 */
  private String meetingLink;

  /** 面试官 */
  private String interviewer;

  /** 状态：0-待确认 1-已确认 2-已完成 3-已取消 */
  private Byte status;

  private String feedback;

  private Byte result;

  private Date createdAt;

  private Date updatedAt;
}


