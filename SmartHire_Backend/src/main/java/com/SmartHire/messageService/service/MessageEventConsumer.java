package com.SmartHire.messageService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.messageService.dto.MessageDTO;
import com.SmartHire.messageService.dto.MessageQueueDTO;
import com.SmartHire.messageService.dto.SendMessageDTO;
import com.SmartHire.messageService.websocket.MessageWebSocket;
import com.SmartHire.common.event.ApplicationCreatedEvent;
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
public class MessageEventConsumer {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ChatMessageService chatMessageService;

  /**
   * 消费聊天消息队列 @RabbitListener 的 bindings 属性会自动创建： 1. 队列（如果不存在） 2. 交换机（如果不存在） 3.
   * 绑定关系 @Queue: 声明队列 -
   * value: 队列名称 - durable: 是否持久化（true = 服务器重启后不丢失） - exclusive: 是否排他（false =
   * 多个消费者可以共享） -
   * autoDelete: 是否自动删除（false = 至少有一个消费者时不会删除） @Exchange: 声明交换机 - value: 交换机名称 -
   * type:
   * 交换机类型（ExchangeTypes.TOPIC = Topic 类型） - durable: 是否持久化
   *
   * <p>
   * key: 路由键
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.CHAT_MESSAGE_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_CHAT))
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

  /**
   * 消费投递/推荐岗位创建事件队列
   * 当求职者投递简历成功后，recruitmentService 会发送此事件
   * messageService 监听此事件，自动发送消息通知HR
   *
   * @param event 投递/推荐岗位创建事件
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.APPLICATION_CREATED_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_APPLICATION_CREATED))
  public void consumeApplicationCreatedEvent(ApplicationCreatedEvent event) {
    try {
      if (event == null || event.getApplicationId() == null) {
        log.warn("收到无效的投递/推荐岗位创建事件: {}", event);
        return;
      }

      log.info(
          "收到投递/推荐岗位创建事件: applicationId={}, jobId={}, seekerId={}, hrId={}",
          event.getApplicationId(),
          event.getJobId(),
          event.getJobSeekerId(),
          event.getHrId());

      // 构建发送消息DTO
      SendMessageDTO sendMessageDTO = new SendMessageDTO();
      // 使用hrUserId而不是hrId，因为receiverId需要的是user表的id
      sendMessageDTO.setReceiverId(event.getHrUserId());
      sendMessageDTO.setApplicationId(event.getApplicationId());
      sendMessageDTO.setMessageType(1); // 文本消息
      sendMessageDTO.setContent(
          event.getMessageContent() != null
              ? event.getMessageContent()
              : "您好，我对这个岗位感兴趣");
      sendMessageDTO.setFileUrl(null);
      sendMessageDTO.setReplyTo(null);
      sendMessageDTO.setFile(null);

      // 发送消息（跳过applicationId验证，因为这是事件触发的消息）
      MessageDTO messageDTO = chatMessageService.sendMessage(event.getSeekerUserId(), sendMessageDTO, true);

      log.info(
          "投递/推荐岗位创建事件处理成功: applicationId={}, messageId={}, conversationId={}",
          event.getApplicationId(),
          messageDTO.getId(),
          messageDTO.getConversationId());
    } catch (Exception e) {
      log.error(
          "消费投递/推荐岗位创建事件失败: applicationId={}, jobId={}",
          event != null ? event.getApplicationId() : null,
          event != null ? event.getJobId() : null,
          e);
      // 注意：事件处理失败不应该影响主业务流程
      // 可以考虑实现重试机制或死信队列
    }
  }
}
