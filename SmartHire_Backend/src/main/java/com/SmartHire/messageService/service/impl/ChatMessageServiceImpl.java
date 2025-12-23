package com.SmartHire.messageService.service.impl;

import com.SmartHire.common.api.ApplicationApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.utils.AliOssUtil;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ChatMessageService;
import com.SmartHire.messageService.service.ConversationService;
import com.SmartHire.messageService.service.MessageEventProducer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
  @Autowired
  private ConversationService conversationService;

  @Autowired
  private MessageEventProducer mqProducerService;

  @Autowired
  private UserAuthApi userAuthApi;

  @Autowired
  private AliOssUtil aliOssUtil;

  @Autowired
  private ApplicationApi applicationApi;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MessageDTO sendMessage(Long senderId, SendMessageDTO dto, boolean skipApplicationValidation) {

    // 检查receiverId对应的用户是否存在
    if (!userAuthApi.existsUser(dto.getReceiverId())) {
      throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
    }

    // 验证用户类型：求职者只能和HR发消息，HR只能和求职者发消息
    validateUserTypeForMessage(senderId, dto.getReceiverId());

    // 验证消息类型
    Integer messageType = dto.getMessageType();
    if (messageType == null) {
      throw new BusinessException(ErrorCode.MESSAGE_TYPE_IS_EMPTY);
    }

    boolean isText = messageType == 1;
    boolean isMedia = 2 <= messageType && messageType <= 5;

    if (isMedia) {
      // 如果没有提供fileUrl，则需要上传文件
      if (dto.getFileUrl() == null || dto.getFileUrl().isBlank()) {
        if (dto.getFile() == null || dto.getFile().isEmpty()) {
          throw new BusinessException(ErrorCode.MEDIA_URL_IS_EMPTY);
        }
        try {
          String objectName = aliOssUtil.generateFileUrl(dto.getFile().getOriginalFilename());
          String url = aliOssUtil.uploadFile("chat", objectName, dto.getFile().getInputStream());
          dto.setFileUrl(url);
        } catch (Exception e) {
          log.error("上传消息附件失败", e);
          throw new BusinessException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
      }
      // 如果媒体消息的content为空，则自动生成
      if (dto.getContent() == null || dto.getContent().isBlank()) {
        dto.setContent(generateMessagePreview(dto.getMessageType(), null));
      }
      // 如果已经提供了fileUrl，则直接使用，不需要上传文件
    } else if (isText) {
      if (dto.getContent() == null || dto.getContent().isBlank()) {
        throw new BusinessException(ErrorCode.CONTENT_IS_EMPTY);
      }
    } else {
      throw new BusinessException(ErrorCode.MESSAGE_TYPE_INVALID);
    }

    log.info("开始获取或创建会话");
    // 1. 获取或创建会话
    Conversation conversation = conversationService.getOrCreateConversation(senderId, dto.getReceiverId());

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

    // 7. 通过消息队列异步推送消息
    mqProducerService.sendChatMessage(dto.getReceiverId(), messageDTO);
    log.info("dto.messageType: {}", dto.getMessageType());
    log.info("message.messageType: {}", message.getMessageType());
    return messageDTO;
  }

  /** 创建 ChatMessage 对象 */
  private static ChatMessage getChatMessage(
      Long senderId, SendMessageDTO dto, Conversation conversation) {
    ChatMessage message = new ChatMessage();
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
    List<Conversation> conversations = conversationService.list(
        new LambdaQueryWrapper<Conversation>()
            .and(
                wrapper -> wrapper
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
  private String generateMessagePreview(Integer messageType, String content) {
    return switch (messageType) {
      case 1 -> // 文本
        content == null ? "" : content.length() > 50 ? content.substring(0, 50) + "..." : content;
      case 2 -> "[图片]";
      case 3 -> "[文件]";
      case 4 -> "[语音]";
      case 5 -> "[视频]";
      default -> content == null ? "" : content;
    };
  }

  /** 转换为 VO */
  private MessageDTO convertToMessageDTO(ChatMessage message) {
    MessageDTO vo = new MessageDTO();
    BeanUtils.copyProperties(message, vo);
    return vo;
  }

  /**
   * 验证用户类型：求职者只能和HR发消息，HR只能和求职者发消息
   *
   * @param senderId   发送者ID
   * @param receiverId 接收者ID
   * @throws BusinessException 如果用户类型不匹配
   */
  private void validateUserTypeForMessage(Long senderId, Long receiverId) {
    // 获取发送者和接收者的用户信息
    var sender = userAuthApi.getUserById(senderId);
    var receiver = userAuthApi.getUserById(receiverId);

    Integer senderType = sender.getUserType();
    Integer receiverType = receiver.getUserType();

    // 验证：求职者(1)只能和HR(2)发消息，HR(2)只能和求职者(1)发消息
    if (senderType == null || receiverType == null) {
      log.warn("用户类型为空，发送者ID: {}, 接收者ID: {}", senderId, receiverId);
      throw new BusinessException(ErrorCode.MESSAGE_USER_TYPE_MISMATCH);
    }

    // 如果发送者是求职者(1)，接收者必须是HR(2)
    if (UserType.SEEKER.getCode().equals(senderType)) {
      if (!UserType.HR.getCode().equals(receiverType)) {
        log.warn(
            "求职者尝试与非HR用户发消息，发送者ID: {}, 接收者ID: {}, 接收者类型: {}", senderId, receiverId, receiverType);
        throw new BusinessException(ErrorCode.MESSAGE_USER_TYPE_MISMATCH);
      }
    }
    // 如果发送者是HR(2)，接收者必须是求职者(1)
    else if (UserType.HR.getCode().equals(senderType)) {
      if (!UserType.SEEKER.getCode().equals(receiverType)) {
        log.warn(
            "HR尝试与非求职者用户发消息，发送者ID: {}, 接收者ID: {}, 接收者类型: {}", senderId, receiverId, receiverType);
        throw new BusinessException(ErrorCode.MESSAGE_USER_TYPE_MISMATCH);
      }
    }
    // 其他类型（如管理员）不允许发送消息
    else {
      log.warn("不支持的用户类型尝试发送消息，发送者ID: {}, 发送者类型: {}", senderId, senderType);
      throw new BusinessException(ErrorCode.MESSAGE_USER_TYPE_MISMATCH);
    }
  }
}
