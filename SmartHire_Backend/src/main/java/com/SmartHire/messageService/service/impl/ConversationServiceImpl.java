package com.SmartHire.messageService.service.impl;

import com.SmartHire.messageService.dto.ConversationDTO;
import com.SmartHire.messageService.mapper.ConversationMapper;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ConversationService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 一对一会话 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
    implements ConversationService {
  /**
   * 获取或创建会话
   *
   * @param user1Id
   * @param user2Id
   * @return
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
    // 统一顺序：较小的ID作为 user1_id
    Long minId = Math.min(user1Id, user2Id);
    Long maxId = Math.max(user1Id, user2Id);

    // 查询是否存在
    LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Conversation::getUser1Id, minId).eq(Conversation::getUser2Id, maxId);
    Conversation conversation = this.getOne(wrapper);

    if (conversation == null) {
      conversation = new Conversation();
      conversation.setUser1Id(minId);
      conversation.setUser2Id(maxId);
      conversation.setUnreadCountUser1(0);
      conversation.setUnreadCountUser2(0);
      conversation.setPinnedByUser1((byte) 0);
      conversation.setPinnedByUser2((byte) 0);
      conversation.setHasNotificationUser1((byte) 0);
      conversation.setHasNotificationUser2((byte) 0);
      conversation.setCreatedAt(new Date());
      conversation.setDeletedByUser1((byte) 0);
      conversation.setDeletedByUser2((byte) 0);
      this.save(conversation);
      log.info("创建会话成功：{}, user1Id={}, user2Id={}", conversation, minId, maxId);
    }
    return conversation;
  }

  @Override
  public List<ConversationDTO> getConversationList(Long userId) {
    // 查询条件：user1_id 或 user2_id 等于当前用户，且未被当前用户删除
    LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
    wrapper.and(
        w ->
            w.eq(Conversation::getUser1Id, userId)
                .eq(Conversation::getDeletedByUser1, (byte) 0)
                .or()
                .eq(Conversation::getUser2Id, userId)
                .eq(Conversation::getDeletedByUser2, (byte) 0));

    wrapper.orderByDesc(Conversation::getLastMessageTime);

    List<Conversation> conversations = this.list(wrapper);

    return conversations.stream()
        .map(conv -> convertToConversationDTO(conv, userId))
        .collect(Collectors.toList());
  }

  // 置顶/取消置顶
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void pinConversation(Long conversationId, Long userId, Boolean pinned) {
    // 查询会话
    Conversation conversation = this.getById(conversationId);
    if (conversation == null) {
      throw new BusinessException(ErrorCode.CONVERSATION_NOT_EXIST);
    }

    // 确定当前用户
    LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(Conversation::getId, conversationId);

    if (conversation.getUser1Id().equals(userId)) {
      updateWrapper.set(Conversation::getPinnedByUser1, pinned ? 1 : 0);
    } else {
      updateWrapper.set(Conversation::getPinnedByUser2, pinned ? 1 : 0);
    }

    this.update(updateWrapper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteConversation(Long conversationId, Long userId) {
    // 查询会话
    Conversation conversation = this.getById(conversationId);
    if (conversation == null) {
      throw new BusinessException(ErrorCode.CONVERSATION_NOT_EXIST);
    }

    // 确定当前用户
    LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(Conversation::getId, conversationId);

    if (conversation.getUser1Id().equals(userId)) {
      updateWrapper.set(Conversation::getDeletedByUser1, 1);
    } else {
      updateWrapper.set(Conversation::getDeletedByUser2, 1);
    }

    this.update(updateWrapper);
  }

  // 转换会话为DTO
  private ConversationDTO convertToConversationDTO(Conversation conversation, Long currentUserId) {
    ConversationDTO dto = new ConversationDTO();
    dto.setId(conversation.getId());

    // 确定对方用户ID
    Long otherUserId =
        conversation.getUser1Id().equals(currentUserId)
            ? conversation.getUser2Id()
            : conversation.getUser1Id();
    dto.setOtherUserId(otherUserId);

    // 获取未读消息数和置顶状态
    if (conversation.getUser1Id().equals(currentUserId)) {
      dto.setUnreadCount(conversation.getUnreadCountUser1());
      dto.setPinned(conversation.getPinnedByUser1());
      dto.setHasNotification(conversation.getHasNotificationUser1());
    } else {
      dto.setUnreadCount(conversation.getUnreadCountUser2());
      dto.setPinned(conversation.getPinnedByUser2());
      dto.setHasNotification(conversation.getHasNotificationUser2());
    }

    dto.setLastMessage(conversation.getLastMessage());
    dto.setLastMessageTime(conversation.getLastMessageTime());

    // 获取对方用户名和用户头像
    String otherUserName =
        this.getBaseMapper().getOtherUserName(conversation.getId(), currentUserId);
    String otherUserAvatar =
        this.getBaseMapper().getOtherUserAvatar(conversation.getId(), currentUserId);
    dto.setOtherUserName(otherUserName);
    dto.setOtherUserAvatar(otherUserAvatar);

    return dto;
  }
}
