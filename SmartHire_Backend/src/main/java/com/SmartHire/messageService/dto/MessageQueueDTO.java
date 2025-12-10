package com.SmartHire.messageService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 消息队列传输对象 用于在 RabbitMQ 中传递消息 */
@Data
@NoArgsConstructor
public class MessageQueueDTO {
  /** 消息类型：CHAT_MESSAGE（聊天消息）、NOTIFICATION（通知） */
  private MessageType messageType;

  /** 接收者用户ID */
  private Long receiverId;

  /** 消息内容（聊天消息时使用） */
  private MessageDTO chatMessage;

  /** 自定义构造函数，避免直接暴露可变对象 */
  public MessageQueueDTO(MessageType messageType, Long receiverId, MessageDTO chatMessage) {
    this.messageType = messageType;
    this.receiverId = receiverId;
    this.chatMessage = copyMessage(chatMessage);
  }

  /** getter 返回防御性拷贝，避免外部修改内部状态 */
  public MessageDTO getChatMessage() {
    return copyMessage(this.chatMessage);
  }

  /** setter 存入防御性拷贝 */
  public void setChatMessage(MessageDTO chatMessage) {
    this.chatMessage = copyMessage(chatMessage);
  }

  /** 消息类型枚举 */
  public enum MessageType {
    CHAT_MESSAGE, // 聊天消息
    NOTIFICATION // 通知（后续扩展）
  }

  /** 创建聊天消息的队列消息 */
  public static MessageQueueDTO createChatMessage(Long receiverId, MessageDTO messageDTO) {
    MessageQueueDTO dto = new MessageQueueDTO();
    dto.setMessageType(MessageType.CHAT_MESSAGE);
    dto.setReceiverId(receiverId);
    dto.setChatMessage(messageDTO);
    return dto;
  }

  /** 防御性拷贝 MessageDTO，避免可变对象泄露 */
  private static MessageDTO copyMessage(MessageDTO source) {
    if (source == null) {
      return null;
    }
    MessageDTO copy = new MessageDTO();
    copy.setId(source.getId());
    copy.setConversationId(source.getConversationId());
    copy.setSenderId(source.getSenderId());
    copy.setReceiverId(source.getReceiverId());
    copy.setMessageType(source.getMessageType());
    copy.setContent(source.getContent());
    copy.setFileUrl(source.getFileUrl());
    copy.setReplyTo(source.getReplyTo());
    copy.setIsRead(source.getIsRead());
    copy.setCreatedAt(
        source.getCreatedAt() == null ? null : new java.util.Date(source.getCreatedAt().getTime()));
    copy.setReplyContent(source.getReplyContent());
    copy.setReplyMessageType(source.getReplyMessageType());
    return copy;
  }
}
