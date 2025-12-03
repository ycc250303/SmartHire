package com.SmartHire.messageService.dto;

import java.util.Date;
import lombok.Data;

@Data
public class ConversationDTO {
  private Long id;
  private Long otherUserId; // 对方用户ID
  private String otherUserName; // 对方用户名（需要 JOIN 查询）
  private String otherUserAvatar; // 对方用户头像
  private String lastMessage; // 最后一条消息预览
  private Date lastMessageTime;
  private Integer unreadCount; // 当前用户的未读数
  private Byte pinned; // 是否置顶
  private Byte hasNotification; // 是否有未读通知
}
