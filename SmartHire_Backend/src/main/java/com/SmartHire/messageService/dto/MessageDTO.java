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
