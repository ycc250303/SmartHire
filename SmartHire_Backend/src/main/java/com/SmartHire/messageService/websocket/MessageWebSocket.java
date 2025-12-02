package com.SmartHire.messageService.websocket;

import com.SmartHire.shared.utils.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ServerEndpoint(
    value = "/message",
    configurator = com.SmartHire.shared.config.WebSocketConfigurator.class)
public class MessageWebSocket {

  // 用户会话
  // 用户ID -> 会话集合
  private static final ConcurrentHashMap<Long, Set<Session>> userSessions =
      new ConcurrentHashMap<>();

  // 会话 -> 用户ID
  // 用于快速获取用户ID
  private static final ConcurrentHashMap<Session, Long> sessionToUserId = new ConcurrentHashMap<>();

  private static JwtUtil jwtUtil;

  @Autowired
  public void setJwtUtil(JwtUtil jwtUtil) {
    MessageWebSocket.jwtUtil = jwtUtil;
  }

  /**
   * 连接建立
   *
   * @param session
   */
  @OnOpen
  public void onOpen(Session session) {
    try {
      // 1. 获取 在 WebSocketConfigurator 中存储的 token
      String token = (String) session.getUserProperties().get("token");
      if (token == null || token.isEmpty()) {
        log.warn("WebSocket 连接缺少 token，关闭连接");
        session.close();
        return;
      }

      // 2. 验证 token 并获取用户信息
      Map<String, Object> claims = jwtUtil.parseToken(token);
      Long userId = jwtUtil.getUserIdFromToken(claims);
      if (userId == null) {
        log.warn("WebSocket token 无效，关闭连接");
        session.close();
        return;
      }

      // 3. 建立映射关系
      session.getUserProperties().put("userId", userId);

      // 4. 添加会话
      userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);

      sessionToUserId.put(session, userId);

      log.info(
          "WebSocket 连接建立: userId={}, sessionId={}, 当前在线用户数={}",
          userId,
          session.getId(),
          userSessions.size());
    } catch (Exception e) {
      log.error("WebSocket 连接建立失败", e);
      try {
        session.close();
      } catch (IOException ex) {
        log.error("关闭连接失败", ex);
      }
    }
  }

  /**
   * 断开连接
   *
   * @param session
   */
  @OnClose
  public void onClose(Session session) {
    Long userId = (Long) session.getUserProperties().get("userId");
    if (userId != null) {
      // 移除会话
      Set<Session> sessions = userSessions.get(userId);
      if (sessions != null) {
        sessions.remove(session);
        if (sessions.isEmpty()) {
          userSessions.remove(userId);
        }
      }
      sessionToUserId.remove(session);
      log.info(
          "WebSocket 断开连接: userId={}, sessionId={}, 当前在线用户数={}",
          userId,
          session.getId(),
          userSessions.size());
    }
  }

  /**
   * 接收消息
   *
   * @param session
   * @param message
   */
  @OnMessage
  public void onMessage(Session session, String message) {
    Long userId = (Long) session.getUserProperties().get("userId");
    log.debug("收到消息: userId={}, message={}", userId, message);

    // 心跳处理：客户端发送 "ping"，服务端回复 "pong"
    if ("ping".equals(message)) {
      try {
        session.getBasicRemote().sendText("pong");
      } catch (IOException e) {
        log.error("发送心跳回复失败", e);
      }
    }
  }

  /**
   * 错误处理
   *
   * @param session
   * @param error
   */
  @OnError
  public void onError(Session session, Throwable error) {
    Long userId = (Long) session.getUserProperties().get("userId");
    log.error("WebSocket 错误: userId={}, error={}", userId, error.getMessage(), error);
  }

  /**
   * 发送消息
   *
   * @param userId
   * @param message
   */
  public static boolean sendMessage(Long userId, String message) {
    Set<Session> sessions = userSessions.get(userId);
    if (sessions == null || sessions.isEmpty()) {
      log.debug("用户 {} 不在线，消息已保存到数据库", userId);
      return false; // 用户不在线，消息已保存，下次上线可拉取
    }

    boolean success = false;
    for (Session session : sessions) {
      try {
        session.getBasicRemote().sendText(message);
        success = true;
      } catch (IOException e) {
        log.error("向用户 {} 推送消息失败: {}", userId, e.getMessage());
      }
    }
    return success;
  }

  /**
   * 检查用户是否在线
   *
   * @param userId
   */
  public static boolean isUserOnline(Long userId) {
    Set<Session> sessions = userSessions.get(userId);
    return sessions != null && !sessions.isEmpty();
  }
}
