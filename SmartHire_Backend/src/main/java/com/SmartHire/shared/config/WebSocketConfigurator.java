package com.SmartHire.shared.config;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
  @Override
  public void modifyHandshake(
      ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
    // 获取请求参数
    String queryString = request.getQueryString();
    if (queryString != null && queryString.startsWith("token=")) {
      String token = queryString.substring(6);
      // 将 token 存储到配置的属性中，后续在 @OnOpen 中可以获取
      config.getUserProperties().put("token", token);
    }
  }
}
