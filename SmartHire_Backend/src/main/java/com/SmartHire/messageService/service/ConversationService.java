package com.SmartHire.messageService.service;

import com.SmartHire.messageService.dto.ConversationDTO;
import com.SmartHire.messageService.model.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 一对一会话 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
public interface ConversationService extends IService<Conversation> {
  /**
   * 获取或创建会话
   *
   * <p>1. 根据两个用户ID获取会话（user1_id 和 user2_id 的顺序可能不同） 2. 如果不存在则创建
   *
   * @param user1Id 用户1ID
   * @param user2Id 用户2ID
   * @return 会话对象
   */
  Conversation getOrCreateConversation(Long user1Id, Long user2Id);

  /**
   * 获取用户的会话列表
   *
   * @param userId 用户ID
   * @return 会话列表（按最后消息时间降序，置顶优先）
   */
  List<ConversationDTO> getConversationList(Long userId);

  /**
   * 置顶/取消置顶会话
   *
   * @param conversationId 会话ID
   * @param userId 用户ID
   * @param pinned 是否置顶
   */
  void pinConversation(Long conversationId, Long userId, Boolean pinned);

  /**
   * 删除会话（单方面）
   *
   * @param conversationId 会话ID
   * @param userId 用户ID
   */
  void deleteConversation(Long conversationId, Long userId);
}
