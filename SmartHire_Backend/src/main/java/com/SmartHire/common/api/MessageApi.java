package com.SmartHire.common.api;

import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;

/**
 * 消息服务API接口 用于模块间通信，避免直接访问数据库
 */
public interface MessageApi {

    /**
     * 删除用户相关的所有消息和会话
     *
     * @param userId 用户ID
     */
    void deleteUserMessages(Long userId);

    /**
     * 发送消息
     *
     * @param senderId 发送者ID
     * @param dto      发送消息参数
     * @return 发送结果
     */
    MessageDTO sendMessage(Long senderId, SendMessageDTO dto);

    /**
     * 获取或创建会话
     *
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @return 会话ID
     */
    Long getOrCreateConversation(Long user1Id, Long user2Id);
}
