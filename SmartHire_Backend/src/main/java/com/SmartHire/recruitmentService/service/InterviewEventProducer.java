package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.InterviewScheduledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 面试事件生产者 服务于将 interview.scheduled 事件发送到消息队列
 *
 * @author SmartHire Team
 */
@Slf4j
@Service
public class InterviewEventProducer {

  @Autowired private RabbitTemplate rabbitTemplate;

  public void publishInterviewScheduled(InterviewScheduledEvent event) {
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.MESSAGE_EXCHANGE, RabbitMQConfig.ROUTING_KEY_INTERVIEW_SCHEDULED, event);
      log.info("面试安排事件已发送: interviewId={}, applicationId={}", event.getInterviewId(), event.getApplicationId());
    } catch (Exception e) {
      log.error("发送面试安排事件失败: interviewId={}, applicationId={}", event.getInterviewId(), event.getApplicationId(), e);
    }
  }
}


