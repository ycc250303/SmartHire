package com.SmartHire.messageService.service.impl;

import com.SmartHire.common.api.MessageApi;
import com.SmartHire.common.dto.messageDto.MessageCommonDTO;
import com.SmartHire.common.dto.messageDto.SendMessageCommonDTO;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.mapper.ConversationMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.messageService.model.Conversation;
import com.SmartHire.messageService.service.ChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public MessageCommonDTO sendMessage(Long senderId, SendMessageCommonDTO dto) {
        if (dto == null) {
            return null;
        }

        // 转换 Common DTO 到内部 DTO
        SendMessageDTO internalDto = new SendMessageDTO();
        BeanUtils.copyProperties(dto, internalDto);

        // 调用内部服务
        MessageDTO result = chatMessageService.sendMessage(senderId, internalDto);

        if (result == null) {
            return null;
        }

        // 转换结果到 Common DTO
        MessageCommonDTO commonDto = new MessageCommonDTO();
        BeanUtils.copyProperties(result, commonDto);
        return commonDto;
    }

    @Override
    public boolean hasMessages(Long conversationId) {
        if (!StringUtils.hasText(String.valueOf(conversationId)))
            return false;
        return chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)) > 0;
    }
}
