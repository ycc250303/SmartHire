package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.ConversationCreatedEvent;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.mapper.ApplicationExtMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 会话事件消费者服务 监听会话创建事件，更新Application记录 */
@Slf4j
@Service
public class ConversationEventConsumer {
  
  @Autowired
  private ApplicationMapper applicationMapper;
  
  @Autowired
  private ApplicationExtMapper applicationExtMapper;

  /**
   * 消费会话创建事件队列
   * 当messageService创建新会话时，recruitmentService会监听此事件，更新对应的Application记录的conversationId
   *
   * @param event 会话创建事件
   */
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitMQConfig.CONVERSATION_CREATED_QUEUE, durable = "true"), exchange = @Exchange(value = RabbitMQConfig.MESSAGE_EXCHANGE, type = ExchangeTypes.TOPIC, durable = "true"), key = RabbitMQConfig.ROUTING_KEY_CONVERSATION_CREATED))
  public void consumeConversationCreatedEvent(ConversationCreatedEvent event) {
    try {
      if (event == null || event.getConversationId() == null || event.getUser1Id() == null || event.getUser2Id() == null) {
        log.warn("收到无效的会话创建事件: {}", event);
        return;
      }

      log.info(
          "收到会话创建事件: conversationId={}, user1Id={}, user2Id={}",
          event.getConversationId(),
          event.getUser1Id(),
          event.getUser2Id());

      // 查找与这两个用户相关的Application记录
      // 通过自定义SQL查询，关联user表、hr_info表、job_seeker表和job_info表
      // 找到与这两个用户相关且没有conversation_id的Application记录
      
      java.util.List<Long> applicationIds = applicationExtMapper.findApplicationIdsByUserIds(
          event.getUser1Id(), event.getUser2Id());
      
      if (applicationIds.isEmpty()) {
        log.debug("没有找到与用户{}和{}相关的Application记录", event.getUser1Id(), event.getUser2Id());
        return;
      }
      
      // 更新这些Application记录的conversationId
      LambdaUpdateWrapper<Application> updateWrapper = new LambdaUpdateWrapper<>();
      updateWrapper
          .in(Application::getId, applicationIds)
          .isNull(Application::getConversationId)
          .set(Application::getConversationId, event.getConversationId());
      
      int updated = applicationMapper.update(null, updateWrapper);
      
      if (updated > 0) {
        log.info("成功更新Application记录的conversationId: conversationId={}, 更新数量={}", 
            event.getConversationId(), updated);
      } else {
        log.debug("没有找到需要更新的Application记录: conversationId={}", event.getConversationId());
      }
    } catch (Exception e) {
      log.error("消费会话创建事件失败: conversationId={}", 
          event != null ? event.getConversationId() : null, e);
    }
  }
}
