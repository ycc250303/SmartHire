package com.SmartHire.common.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** RabbitMQ 配置类 定义交换机、队列和绑定关系 */
@Configuration
public class RabbitMQConfig {

  // ========== 常量定义（供生产者和消费者使用）==========

  /** 消息交换机 */
  public static final String MESSAGE_EXCHANGE = "smarthire.message.exchange";

  /** 聊天消息队列 */
  public static final String CHAT_MESSAGE_QUEUE = "smarthire.chat.message.queue";

  /** 通知消息队列（预留） */
  public static final String NOTIFICATION_QUEUE = "smarthire.notification.queue";

  /** 聊天消息路由键 */
  public static final String ROUTING_KEY_CHAT = "message.chat";

  /** 通知消息路由键（预留） */
  public static final String ROUTING_KEY_NOTIFICATION = "message.notification";

  /** 配置消息转换器（JSON 格式） 将 Java 对象自动序列化为 JSON，消费时自动反序列化 */
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /** 配置 RabbitTemplate（消息发送模板） 使用 JSON 转换器，自动序列化消息 */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    template.setConfirmCallback(
        (correlationData, ack, cause) -> {
          if (!ack) {
            System.err.println("消息发送失败：" + cause);
          }
        });
    return template;
  }

  /** 配置监听器容器工厂 消费者使用 JSON 转换器自动反序列化消息 */
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter());
    // 设置并发消费者数量
    factory.setConcurrentConsumers(3);
    factory.setMaxConcurrentConsumers(10);
    return factory;
  }
}
