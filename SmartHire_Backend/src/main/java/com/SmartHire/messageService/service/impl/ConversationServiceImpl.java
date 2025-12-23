package com.SmartHire.messageService.service.impl;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.messageService.dto.ConversationDTO;
import com.SmartHire.messageService.mapper.ConversationMapper;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ConversationService;
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
    } else {
      // 软删除场景：若双方都已删除，则“复活”会话并重置计数/置顶/通知
      if (conversation.getDeletedByUser1() == 1 && conversation.getDeletedByUser2() == 1) {
        LambdaUpdateWrapper<Conversation> revive = new LambdaUpdateWrapper<>();
        revive
            .eq(Conversation::getId, conversation.getId())
            .set(Conversation::getDeletedByUser1, 0)
            .set(Conversation::getDeletedByUser2, 0)
            .set(Conversation::getUnreadCountUser1, 0)
            .set(Conversation::getUnreadCountUser2, 0)
            .set(Conversation::getPinnedByUser1, 0)
            .set(Conversation::getPinnedByUser2, 0)
            .set(Conversation::getHasNotificationUser1, 0)
            .set(Conversation::getHasNotificationUser2, 0)
            .set(Conversation::getLastMessage, null)
            .set(Conversation::getLastMessageTime, null);
        this.update(revive);
        // 同步内存对象，避免后续使用旧值
        conversation.setDeletedByUser1((byte) 0);
        conversation.setDeletedByUser2((byte) 0);
        conversation.setUnreadCountUser1(0);
        conversation.setUnreadCountUser2(0);
        conversation.setPinnedByUser1((byte) 0);
        conversation.setPinnedByUser2((byte) 0);
        conversation.setHasNotificationUser1((byte) 0);
        conversation.setHasNotificationUser2((byte) 0);
        conversation.setLastMessage(null);
        conversation.setLastMessageTime(null);
        log.info("复活已双方删除的会话：id={}, user1Id={}, user2Id={}", conversation.getId(), minId, maxId);
      } else {
        // 若仅当前请求方曾删除，则取消其删除标记
        boolean isCallerUser1 = conversation.getUser1Id().equals(user1Id);
        boolean isCallerUser2 = conversation.getUser2Id().equals(user1Id);

        if (isCallerUser1 && conversation.getDeletedByUser1() == 1) {
          this.update(
              new LambdaUpdateWrapper<Conversation>()
                  .eq(Conversation::getId, conversation.getId())
                  .set(Conversation::getDeletedByUser1, 0));
          conversation.setDeletedByUser1((byte) 0);
          log.info("用户 {} 重新打开会话，清除 deleted_by_user1 标记，会话ID={}", user1Id, conversation.getId());
        }

        if (isCallerUser2 && conversation.getDeletedByUser2() == 1) {
          this.update(
              new LambdaUpdateWrapper<Conversation>()
                  .eq(Conversation::getId, conversation.getId())
                  .set(Conversation::getDeletedByUser2, 0));
          conversation.setDeletedByUser2((byte) 0);
          log.info("用户 {} 重新打开会话，清除 deleted_by_user2 标记，会话ID={}", user1Id, conversation.getId());
        }
      }
    }
    return conversation;
  }

  @Override
  public List<ConversationDTO> getConversationList(Long userId) {
    // 查询条件：user1_id 或 user2_id 等于当前用户，且未被当前用户删除
    LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
    wrapper.and(
        w -> w.eq(Conversation::getUser1Id, userId)
            .eq(Conversation::getDeletedByUser1, (byte) 0)
            .or()
            .eq(Conversation::getUser2Id, userId)
            .eq(Conversation::getDeletedByUser2, (byte) 0));

    // 先按最后消息时间降序查询
    wrapper.orderByDesc(Conversation::getLastMessageTime);

    List<Conversation> conversations = this.list(wrapper);

    // 转换为 DTO 并排序：置顶优先，然后按最后消息时间降序
    return conversations.stream()
        .map(conv -> convertToConversationDTO(conv, userId))
        .sorted(
            (dto1, dto2) -> {
              // 先按置顶状态排序（置顶的在前，1 > 0）
              int pinnedCompare = Byte.compare(
                  dto2.getPinned() != null ? dto2.getPinned() : 0,
                  dto1.getPinned() != null ? dto1.getPinned() : 0);
              if (pinnedCompare != 0) {
                return pinnedCompare;
              }
              // 置顶状态相同时，按最后消息时间降序
              if (dto1.getLastMessageTime() == null && dto2.getLastMessageTime() == null) {
                return 0;
              }
              if (dto1.getLastMessageTime() == null) {
                return 1;
              }
              if (dto2.getLastMessageTime() == null) {
                return -1;
              }
              return dto2.getLastMessageTime().compareTo(dto1.getLastMessageTime());
            })
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

  // 删除会话
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
    Long otherUserId = conversation.getUser1Id().equals(currentUserId)
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
    String otherUserName = this.getBaseMapper().getOtherUserName(conversation.getId(), currentUserId);
    String otherUserAvatar = this.getBaseMapper().getOtherUserAvatar(conversation.getId(), currentUserId);
    dto.setOtherUserName(otherUserName);
    dto.setOtherUserAvatar(otherUserAvatar);

    // 对方公司信息（若对方为HR，否则可能为null）
    Long otherCompanyId = this.getBaseMapper().getOtherCompanyId(conversation.getId(), currentUserId);
    dto.setOtherCompanyId(otherCompanyId);
    if (otherCompanyId != null) {
      dto.setOtherCompanyName(
          this.getBaseMapper().getOtherCompanyName(conversation.getId(), currentUserId));
      dto.setOtherCompanyLogo(
          this.getBaseMapper().getOtherCompanyLogo(conversation.getId(), currentUserId));
    }

    return dto;
  }
}
