package com.SmartHire.common.api;

import com.SmartHire.recruitmentService.model.Application;

/** 投递记录服务API接口 用于模块间通信，避免直接访问数据库 */
public interface ApplicationApi {

  /**
   * 根据投递ID获取投递记录
   *
   * @param applicationId 投递ID
   * @return 投递记录
   */
  Application getApplicationById(Long applicationId);

  /**
   * 验证投递记录是否存在
   *
   * @param applicationId 投递ID
   * @return 投递记录是否存在
   */
  boolean existsApplication(Long applicationId);

  /**
   * 根据投递ID获取会话ID
   *
   * @param applicationId 投递ID
   * @return 会话ID
   */
  Long getConversationIdByApplicationId(Long applicationId);
}
