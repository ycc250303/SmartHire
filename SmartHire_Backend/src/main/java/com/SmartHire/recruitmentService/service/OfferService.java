package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.OfferRequest;
import com.SmartHire.recruitmentService.dto.OfferResponseDTO;

/**
 * Offer 服务接口
 */
public interface OfferService {
  /**
   * 发送 Offer（或仅保存草稿，若 send=false 则不更新 application.status）
   *
   * @param request Offer 请求
   * @return applicationId
   */
  Long sendOffer(OfferRequest request);

  /**
   * 响应 Offer（求职者调用）
   *
   * @param request 响应请求
   */
  void respondToOffer(OfferResponseDTO request);

  /**
   * 获取 Offer 状态
   *
   * @param offerId Offer ID
   * @return Offer 状态：0-待接受 1-已接受 2-已拒绝
   */
  Integer getOfferStatus(Long offerId);
}
