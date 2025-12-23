package com.SmartHire.messageService.service.impl;

import com.SmartHire.common.api.MessageApi;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.mapper.ConversationMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息服务API实现类 用于模块间通信
 */
@Slf4j
@Service
public class MessageApiImpl implements MessageApi {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserMessages(Long userId) {
        log.info("开始删除用户相关的消息和会话，用户ID：{}", userId);

        // 1. 删除用户发送或接收的所有消息
        LambdaQueryWrapper<ChatMessage> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(ChatMessage::getSenderId, userId).or().eq(ChatMessage::getReceiverId, userId);
        int deletedMessages = chatMessageMapper.delete(messageWrapper);
        log.info("删除用户消息记录数：{}", deletedMessages);

        // 2. 删除用户相关的所有会话
        LambdaQueryWrapper<Conversation> conversationWrapper = new LambdaQueryWrapper<>();
        conversationWrapper
                .eq(Conversation::getUser1Id, userId)
                .or()
                .eq(Conversation::getUser2Id, userId);
        int deletedConversations = conversationMapper.delete(conversationWrapper);
        log.info("删除用户会话记录数：{}", deletedConversations);

        log.info("删除用户消息和会话完成，用户ID：{}", userId);
    }

    @Override
    public MessageDTO sendMessage(Long senderId, SendMessageDTO dto) {
        return chatMessageService.sendMessage(senderId, dto);
    }
}
