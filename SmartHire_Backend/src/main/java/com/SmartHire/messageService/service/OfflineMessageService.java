package com.SmartHire.messageService.service;

import jakarta.websocket.Session;

public interface OfflineMessageService {
  void pushUnreadMessages(Long userId, Session session);
}
