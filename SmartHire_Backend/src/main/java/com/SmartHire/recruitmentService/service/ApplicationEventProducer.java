package com.SmartHire.recruitmentService.service;

import com.SmartHire.common.config.RabbitMQConfig;
import com.SmartHire.common.event.ApplicationCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 投递/推荐岗位事件生产者服务
 * 负责发送投递/推荐岗位相关的领域事件到消息队列，实现服务间异步解耦
 *
 * @author SmartHire Team
 */
@Slf4j
@Service
public class ApplicationEventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送投递/推荐岗位创建事件
     * 当求职者投递简历成功后，发送此事件通知其他服务（如消息服务）
     *
     * @param event 投递/推荐岗位创建事件
     */
    public void publishApplicationCreated(ApplicationCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MESSAGE_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_APPLICATION_CREATED,
                    event);
            log.info(
                    "投递/推荐岗位创建事件已发送到队列: applicationId={}, jobId={}, seekerId={}",
                    event.getApplicationId(),
                    event.getJobId(),
                    event.getJobSeekerId());
        } catch (Exception e) {
            log.error(
                    "发送投递/推荐岗位创建事件失败: applicationId={}, jobId={}",
                    event.getApplicationId(),
                    event.getJobId(),
                    e);
            // 注意：事件发送失败不应该影响主业务流程，只记录日志
            // 如果需要保证事件必达，可以实现重试机制或死信队列
        }
    }
}
