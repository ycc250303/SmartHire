package com.SmartHire.common.api;

import com.SmartHire.common.dto.messageDto.MessageCommonDTO;
import com.SmartHire.common.dto.messageDto.SendMessageCommonDTO;

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
    MessageCommonDTO sendMessage(Long senderId, SendMessageCommonDTO dto);

    /**
     * 检查会话是否包含有效消息
     * 
     * @param conversationId 会话ID
     * @return 如果会话存在且有消息则返回true
     */
    boolean hasMessages(Long conversationId);
}
