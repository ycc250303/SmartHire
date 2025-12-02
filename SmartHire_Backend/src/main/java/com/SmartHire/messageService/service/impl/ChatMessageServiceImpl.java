package com.SmartHire.messageService.service.impl;

import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ChatMessageService;
import com.SmartHire.messageService.service.ConversationService;
import com.SmartHire.messageService.websocket.MessageWebSocket;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天消息表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {
  @Autowired private ConversationService conversationService;

  @Autowired private ObjectMapper objectMapper; // JSON 序列化工具

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MessageDTO sendMessage(Long senderId, SendMessageDTO dto) {
    // 1. 获取或创建会话
    Conversation conversation =
        conversationService.getOrCreateConversation(senderId, dto.getReceiverId());

    // 2. 创建消息对象

    ChatMessage message = getChatMessage(senderId, dto, conversation);

    // 3. 保存消息到数据库
    this.save(message);

    // 4. 更新会话的最后消息和时间
    Conversation updateConv = new Conversation();
    updateConv.setId(conversation.getId());
    // 生成消息预览（文本消息取前 50 字符，其他类型显示类型描述）
    String preview = generateMessagePreview(dto.getMessageType(), dto.getContent());
    updateConv.setLastMessage(preview);
    updateConv.setLastMessageTime(new Date());

    // 5. 更新未读数（接收者的未读数+1）
    // 判断接收者是 user1 还是 user2
    if (conversation.getUser1Id().equals(dto.getReceiverId())) {
      // 接收者是 user1，更新 unread_count_user1
      updateConv.setUnreadCountUser1(
          (conversation.getUnreadCountUser1() == null ? 0 : conversation.getUnreadCountUser1())
              + 1);
      updateConv.setHasNotificationUser1((byte) 1);
    } else {
      // 接收者是 user2，更新 unread_count_user2
      updateConv.setUnreadCountUser2(
          (conversation.getUnreadCountUser2() == null ? 0 : conversation.getUnreadCountUser2())
              + 1);
      updateConv.setHasNotificationUser2((byte) 1);
    }
    conversationService.updateById(updateConv);

    // 6. 返回消息数据
    MessageDTO messageDTO = convertToMessageDTO(message);

    // 7. 通过 WebSocket 推送给接收者
    try {
      String jsonMessage = objectMapper.writeValueAsString(messageDTO);
      boolean pushed = MessageWebSocket.sendMessage(dto.getReceiverId(), jsonMessage);
      if (pushed) {
        log.info("消息已实时推送给用户: receiverId={}, messageId={}", dto.getReceiverId(), message.getId());
      }
    } catch (Exception e) {
      log.error("推送消息失败，但消息已保存到数据库", e);
      // 不抛异常，保证业务逻辑不受影响
    }

    return messageDTO;
  }

  /** 创建 ChatMessage 对象 */
  private static ChatMessage getChatMessage(
      Long senderId, SendMessageDTO dto, Conversation conversation) {
    ChatMessage message = new ChatMessage();
    message.setApplicationId(dto.getApplicationId());
    message.setSenderId(senderId);
    message.setReceiverId(dto.getReceiverId());
    message.setConversationId(conversation.getId());
    message.setMessageType(dto.getMessageType());
    message.setContent(dto.getContent());
    message.setFileUrl(dto.getFileUrl());
    message.setReplyTo(dto.getReplyTo());
    message.setIsRead((byte) 0); // 默认未读
    message.setIsDeleted((byte) 0);
    message.setCreatedAt(new Date());
    return message;
  }

  @Override
  public List<MessageDTO> getChatHistory(
      Long conversationId, Long userId, Integer page, Integer size) {
    // 1. 构建分页对象
    Page<ChatMessage> pageObj = new Page<>(page, size);

    // 2. 构建查询条件
    LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .eq(ChatMessage::getConversationId, conversationId)
        .eq(ChatMessage::getIsDeleted, 0) // 过滤已删除的消息
        .orderByDesc(ChatMessage::getCreatedAt); // 按时间降序（最新的在前）

    // 3. 分页查询
    Page<ChatMessage> result = this.page(pageObj, wrapper);

    // 4. 转换为 VO
    return result.getRecords().stream().map(this::convertToMessageDTO).collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void markAsRead(Long conversationId, Long userId) {
    // 1. 批量更新消息为已读
    LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper
        .eq(ChatMessage::getConversationId, conversationId)
        .eq(ChatMessage::getReceiverId, userId)
        .eq(ChatMessage::getIsRead, 0) // 未读消息
        .set(ChatMessage::getIsRead, 1);
    this.update(updateWrapper);

    // 2. 更新会话的未读数
    Conversation conversation = conversationService.getById(conversationId);
    if (conversation != null) {
      Conversation updateConv = new Conversation();
      updateConv.setId(conversationId);

      if (conversation.getUser1Id().equals(userId)) {
        updateConv.setUnreadCountUser1(0);
        updateConv.setHasNotificationUser1((byte) 0);
      } else {
        updateConv.setUnreadCountUser2(0);
        updateConv.setHasNotificationUser2((byte) 0);
      }
      conversationService.updateById(updateConv);
    }
  }

  @Override
  public Integer getUnreadCount(Long userId) {
    List<Conversation> conversations =
        conversationService.list(
            new LambdaQueryWrapper<Conversation>()
                .and(
                    wrapper ->
                        wrapper
                            .eq(Conversation::getUser1Id, userId)
                            .or()
                            .eq(Conversation::getUser2Id, userId)));

    int total = 0;
    for (Conversation conv : conversations) {
      if (conv.getUser1Id().equals(userId)) {
        total += (conv.getUnreadCountUser1() == null ? 0 : conv.getUnreadCountUser1());
      } else {
        total += (conv.getUnreadCountUser2() == null ? 0 : conv.getUnreadCountUser2());
      }
    }

    return total;
  }

  /** 生成消息预览 */
  private String generateMessagePreview(Byte messageType, String content) {
    if (content == null) {
      return "";
    }
    return switch (messageType) {
      case 1 -> // 文本
      content.length() > 50 ? content.substring(0, 50) + "..." : content;
      case 2 -> "[图片]";
      case 3 -> "[文件]";
      case 4 -> "[语音]";
      case 5 -> "[视频]";
      default -> content;
    };
  }

  /** 转换为 VO */
  private MessageDTO convertToMessageDTO(ChatMessage message) {
    MessageDTO vo = new MessageDTO();
    BeanUtils.copyProperties(message, vo);
    return vo;
  }
}
