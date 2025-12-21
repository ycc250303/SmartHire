package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.OfferRequest;

/**
 * Offer 服务接口（最小可行：不建表）
 */
public interface OfferService {
  /**
   * 发送 Offer（或仅保存草稿，若 send=false 则不更新 application.status）
   *
   * @param request Offer 请求
   * @return applicationId
   */
  Long sendOffer(OfferRequest request);
}


