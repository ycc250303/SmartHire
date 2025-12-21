package com.SmartHire.messageService.service;

import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.model.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 聊天消息表 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
public interface ChatMessageService extends IService<ChatMessage> {
  /**
   * 发送消息
   *
   * @param senderId                  发送者ID
   * @param dto                       发送消息参数
   * @param skipApplicationValidation 是否跳过applicationId验证，默认为false。设置为true用于事件驱动的场景，避免跨服务同步调用
   * @return 发送结果
   */
  MessageDTO sendMessage(Long senderId, SendMessageDTO dto, boolean skipApplicationValidation);

  /**
   * 发送消息（默认不跳过applicationId验证）
   *
   * @param senderId 发送者ID
   * @param dto      发送消息参数
   * @return 发送结果
   */
  default MessageDTO sendMessage(Long senderId, SendMessageDTO dto) {
    return sendMessage(senderId, dto, false);
  }

  /**
   * 获取聊天记录
   *
   * @param conversationId 会话ID
   * @param userId         用户ID
   * @param page           页码
   * @param size           每页数量
   * @return 聊天记录
   */
  List<MessageDTO> getChatHistory(Long conversationId, Long userId, Integer page, Integer size);

  /**
   * 获取未读消息数量
   *
   * @param userId 用户ID
   * @return 未读消息数量
   */
  Integer getUnreadCount(Long userId);

  /**
   * 标记消息已读
   *
   * @param conversationId 会话ID
   * @param userId         用户ID
   * @return 标记结果
   */
  void markAsRead(Long conversationId, Long userId);
}
