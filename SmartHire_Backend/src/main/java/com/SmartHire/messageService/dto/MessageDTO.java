package com.SmartHire.messageService.dto;

import java.util.Date;
import lombok.Data;

// 消息数据传输对象
@Data
public class MessageDTO {
  private Long id;
  private Long conversationId;
  private Long senderId;
  private Long receiverId;
  /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 8-面试邀请 9-Offer 10-拒绝通知 */
  private Integer messageType;
  private String content;
  private String fileUrl;
  private Long replyTo;
  private Byte isRead;
  private Date createdAt;

  // 可选：被引用消息的预览（如果 replyTo 不为空）
  private String replyContent;
  private Integer replyMessageType;
}
