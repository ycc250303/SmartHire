package com.SmartHire.messageService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.MessageQueueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 消息队列生产者服务 负责将消息发送到 RabbitMQ 队列 */
@Slf4j
@Service
public class MQProducerService {
  @Autowired private RabbitTemplate rabbitTemplate;

  /**
   * 发送聊天消息到队列
   *
   * @param receiverId 接收者用户ID
   * @param messageDTO 消息内容
   */
  public void sendChatMessage(Long receiverId, MessageDTO messageDTO) {
    try {
      MessageQueueDTO queueMessage = MessageQueueDTO.createChatMessage(receiverId, messageDTO);
      // 发送到队列
      // 参数：交换机名称、路由键、消息对象
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.MESSAGE_EXCHANGE, RabbitMQConfig.ROUTING_KEY_CHAT, queueMessage);

      log.info("聊天消息已发送到队列: receiverId={}, messageId={}", receiverId, messageDTO.getId());
    } catch (Exception e) {
      log.error("发送聊天消息到队列失败: receiverId={}, messageId={}", receiverId, messageDTO.getId(), e);
    }
  }
}
