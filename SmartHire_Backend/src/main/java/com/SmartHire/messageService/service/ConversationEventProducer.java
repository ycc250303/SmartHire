package com.SmartHire.messageService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.ConversationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 会话事件生产者服务 负责将会话创建事件发送到 RabbitMQ 队列 */
@Slf4j
@Service
public class ConversationEventProducer {
  @Autowired private RabbitTemplate rabbitTemplate;

  /**
   * 发送会话创建事件到队列
   *
   * @param event 会话创建事件
   */
  public void sendConversationCreatedEvent(ConversationCreatedEvent event) {
    try {
      // 发送到队列
      // 参数：交换机名称、路由键、消息对象
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.MESSAGE_EXCHANGE, RabbitMQConfig.ROUTING_KEY_CONVERSATION_CREATED, event);

      log.info("会话创建事件已发送到队列: conversationId={}, user1Id={}, user2Id={}", 
          event.getConversationId(), event.getUser1Id(), event.getUser2Id());
    } catch (Exception e) {
      log.error("发送会话创建事件到队列失败: conversationId={}", event.getConversationId(), e);
    }
  }
}
