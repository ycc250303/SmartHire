package com.SmartHire.common.api;

/** 消息服务API接口 用于模块间通信，避免直接访问数据库 */
public interface MessageApi {

  /**
   * 删除用户相关的所有消息和会话
   *
   * @param userId 用户ID
   */
  void deleteUserMessages(Long userId);
}
