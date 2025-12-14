package com.SmartHire.messageService.service.impl;

import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.messageService.service.OfflineMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OfflineMessageServiceImpl implements OfflineMessageService {
  @Autowired private ChatMessageMapper chatMessageMapper;

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void pushUnreadMessages(Long userId, Session session) {
    List<ChatMessage> unreadList =
        chatMessageMapper.selectList(
            new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, false)
                .orderByAsc(ChatMessage::getCreatedAt));

    if (unreadList.isEmpty()) return;

    for (ChatMessage msg : unreadList) {
      try {
        MessageDTO dto = convertToDTO(msg);

        // 推送给当前新建立的 session
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(dto));
      } catch (Exception e) {
        log.error("补偿离线消息失败 userId={}, msgId={}", userId, msg.getId(), e);
      }
    }
  }

  private MessageDTO convertToDTO(ChatMessage message) {
    MessageDTO dto = new MessageDTO();
    BeanUtils.copyProperties(message, dto);
    return dto;
  }
}
