package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.ApplicationRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationRejectedEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishApplicationRejected(ApplicationRejectedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.MESSAGE_EXCHANGE, RabbitMQConfig.ROUTING_KEY_APPLICATION_REJECTED, event);
            log.info("ApplicationRejectedEvent 已发送: applicationId={}", event.getApplicationId());
        } catch (Exception e) {
            log.error("发送 ApplicationRejectedEvent 失败: applicationId={}", event.getApplicationId(), e);
        }
    }
}


