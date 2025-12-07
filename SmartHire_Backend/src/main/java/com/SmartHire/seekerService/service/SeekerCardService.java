package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.SeekerCardDTO;

/** 求职卡片查询服务。 */
public interface SeekerCardService {
  /**
   * 查询指定用户的求职卡片信息。
   *
   * @param userId 用户ID
   * @return 求职卡片数据
   */
  SeekerCardDTO getJobCard(Long userId);
}
