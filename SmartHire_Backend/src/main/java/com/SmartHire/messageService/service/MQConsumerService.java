package com.SmartHire.messageService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.MessageQueueDTO;
import com.SmartHire.messageService.websocket.MessageWebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 消息队列消费者服务 使用 @RabbitListener 注解声明队列、交换机和绑定关系 */
@Slf4j
@Service
public class MQConsumerService {
  @Autowired private ObjectMapper objectMapper;

  /**
   * 消费聊天消息队列 @RabbitListener 的 bindings 属性会自动创建： 1. 队列（如果不存在） 2. 交换机（如果不存在） 3. 绑定关系 @Queue: 声明队列 -
   * value: 队列名称 - durable: 是否持久化（true = 服务器重启后不丢失） - exclusive: 是否排他（false = 多个消费者可以共享） -
   * autoDelete: 是否自动删除（false = 至少有一个消费者时不会删除） @Exchange: 声明交换机 - value: 交换机名称 - type:
   * 交换机类型（ExchangeTypes.TOPIC = Topic 类型） - durable: 是否持久化
   *
   * <p>key: 路由键
   */
  @RabbitListener(
      bindings =
          @QueueBinding(
              value = @Queue(value = RabbitMQConfig.CHAT_MESSAGE_QUEUE, durable = "true"),
              exchange =
                  @Exchange(
                      value = RabbitMQConfig.MESSAGE_EXCHANGE,
                      type = ExchangeTypes.TOPIC,
                      durable = "true"),
              key = RabbitMQConfig.ROUTING_KEY_CHAT))
  public void consumeChatMessage(MessageQueueDTO queueMessage) {
    try {
      if (queueMessage.getMessageType() != MessageQueueDTO.MessageType.CHAT_MESSAGE) {
        log.warn("收到非聊天消息类型: {}", queueMessage.getMessageType());
        return;
      }

      Long receiverId = queueMessage.getReceiverId();
      MessageDTO messageDTO = queueMessage.getChatMessage();

      if (receiverId == null || messageDTO == null) {
        log.warn("消息数据不完整: receiverId={}, messageDTO={}", receiverId, messageDTO);
        return;
      }

      String jsonMessage = objectMapper.writeValueAsString(messageDTO);
      boolean pushed = MessageWebSocket.sendMessage(receiverId, jsonMessage);

      if (pushed) {
        log.info("消息已通过队列推送给用户: receiverId={}, messageId={}", receiverId, messageDTO.getId());
      } else {
        log.debug("用户不在线，消息已保存: receiverId={}, messageId={}", receiverId, messageDTO.getId());
      }
    } catch (Exception e) {
      log.error("消费聊天消息失败: receiverId={}", queueMessage.getReceiverId(), e);
    }
  }
}
