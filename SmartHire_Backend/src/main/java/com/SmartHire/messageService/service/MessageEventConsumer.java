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

  /**
   * 消费面试安排事件，发送面试通知给求职者（HR -> 求职者）
   *
   * @param event 面试安排事件
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.INTERVIEW_SCHEDULED_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_INTERVIEW_SCHEDULED))
  public void consumeInterviewScheduledEvent(com.SmartHire.common.event.InterviewScheduledEvent event) {
    try {
      if (event == null || event.getInterviewId() == null) {
        log.warn("收到无效的面试安排事件: {}", event);
        return;
      }

      log.info(
          "收到面试安排事件: interviewId={}, applicationId={}, seekerUserId={}",
          event.getInterviewId(),
          event.getApplicationId(),
          event.getSeekerUserId());

      // 构建发送消息DTO，由 HR 作为 sender 发送给求职者
      SendMessageDTO sendMessageDTO = new SendMessageDTO();
      sendMessageDTO.setReceiverId(event.getSeekerUserId());
      sendMessageDTO.setMessageType(1); // 文本消息

      // 构建通知内容
      String content =
          String.format(
              "您好，您有一场新的面试安排：时间：%s，地点：%s，面试官：%s。请按时参加。如有问题请联系HR。",
              event.getInterviewTime() != null ? event.getInterviewTime().toString() : "未指定",
              event.getLocation() != null ? event.getLocation() : "未指定",
              event.getInterviewer() != null ? event.getInterviewer() : "未指定");

      // 如果事件携带自定义 note，优先使用
      if (event.getNote() != null && !event.getNote().isEmpty()) {
        content = event.getNote();
      }

      sendMessageDTO.setContent(content);
      sendMessageDTO.setFileUrl(null);
      sendMessageDTO.setReplyTo(null);
      sendMessageDTO.setFile(null);

      // 以 hrUserId 为 sender 发送给 seekerUserId
      chatMessageService.sendMessage(event.getHrUserId(), sendMessageDTO, true);

      log.info("面试通知已发送: interviewId={}, seekerUserId={}", event.getInterviewId(), event.getSeekerUserId());
    } catch (Exception e) {
      log.error("消费面试安排事件失败: interviewId={}, applicationId={}", event != null ? event.getInterviewId() : null, event != null ? event.getApplicationId() : null, e);
    }
  }

  /**
   * 消费 Offer 发送事件，发送 Offer 通知给求职者（HR -> 求职者）
   *
   * @param event OfferSentEvent
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.OFFER_SENT_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_OFFER_SENT))
  public void consumeOfferSentEvent(com.SmartHire.common.event.OfferSentEvent event) {
    try {
      if (event == null || event.getApplicationId() == null) {
        log.warn("收到无效的 OfferSentEvent: {}", event);
        return;
      }

      log.info(
          "收到 OfferSentEvent: applicationId={}, seekerUserId={}, hrUserId={}",
          event.getApplicationId(),
          event.getSeekerUserId(),
          event.getHrUserId());

      SendMessageDTO sendMessageDTO = new SendMessageDTO();
      sendMessageDTO.setReceiverId(event.getSeekerUserId());
      sendMessageDTO.setMessageType(1); // 文本消息

      StringBuilder content = new StringBuilder();
      content.append("恭喜，您收到录用通知！");
      if (event.getTitle() != null) {
        content.append(" 职位：").append(event.getTitle()).append("；");
      }
      if (event.getBaseSalary() != null) {
        content.append(" 薪资：").append(event.getBaseSalary()).append("；");
      }
      if (event.getStartDate() != null) {
        content.append(" 到岗：").append(event.getStartDate().toString()).append("；");
      }
      if (event.getNote() != null && !event.getNote().isEmpty()) {
        content.append(" 备注：").append(event.getNote());
      }

      sendMessageDTO.setContent(content.toString());
      sendMessageDTO.setFileUrl(null);
      sendMessageDTO.setReplyTo(null);
      sendMessageDTO.setFile(null);

      chatMessageService.sendMessage(event.getHrUserId(), sendMessageDTO, true);

      log.info("Offer 通知已发送: applicationId={}, seekerUserId={}", event.getApplicationId(), event.getSeekerUserId());
    } catch (Exception e) {
      log.error("消费 OfferSentEvent 失败: applicationId={}", event != null ? event.getApplicationId() : null, e);
    }
  }

  /**
   * 消费 Application 被拒事件，发送拒绝通知给求职者
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.APPLICATION_REJECTED_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_APPLICATION_REJECTED))
  public void consumeApplicationRejectedEvent(com.SmartHire.common.event.ApplicationRejectedEvent event) {
    try {
      if (event == null || event.getApplicationId() == null) {
        log.warn("收到无效的 ApplicationRejectedEvent: {}", event);
        return;
      }

      log.info("收到拒绝事件: applicationId={}, seekerUserId={}", event.getApplicationId(), event.getSeekerUserId());

      SendMessageDTO sendMessageDTO = new SendMessageDTO();
      sendMessageDTO.setReceiverId(event.getSeekerUserId());
      sendMessageDTO.setMessageType(1);

      String content = "很抱歉，您的本次应聘未通过。";
      if (event.getReason() != null && !event.getReason().isEmpty()) {
        content += " 原因：" + event.getReason();
      }

      sendMessageDTO.setContent(content);
      sendMessageDTO.setFileUrl(null);
      sendMessageDTO.setReplyTo(null);
      sendMessageDTO.setFile(null);

      chatMessageService.sendMessage(event.getHrUserId(), sendMessageDTO, true);
      log.info("拒绝通知已发送: applicationId={}, seekerUserId={}", event.getApplicationId(), event.getSeekerUserId());
    } catch (Exception e) {
      log.error("消费 ApplicationRejectedEvent 失败: applicationId={}", event != null ? event.getApplicationId() : null, e);
    }
  }
}
