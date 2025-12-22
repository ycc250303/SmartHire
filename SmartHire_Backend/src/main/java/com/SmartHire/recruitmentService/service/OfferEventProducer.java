package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.OfferSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Offer 事件生产者
 */
@Slf4j
@Service
public class OfferEventProducer {

  @Autowired private RabbitTemplate rabbitTemplate;

  public void publishOfferSent(OfferSentEvent event) {
    try {
      rabbitTemplate.convertAndSend(RabbitMQConfig.MESSAGE_EXCHANGE, RabbitMQConfig.ROUTING_KEY_OFFER_SENT, event);
      log.info("OfferSentEvent 已发送: applicationId={}", event.getApplicationId());
    } catch (Exception e) {
      log.error("发送 OfferSentEvent 失败: applicationId={}", event.getApplicationId(), e);
    }
  }
}


