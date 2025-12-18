package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.SeekerCardDTO;

import java.util.List;

/** 求职卡片查询服务。 */
public interface SeekerCardService {
  /**
   * 查询指定用户的求职卡片信息。
   *
   * @param userId 用户ID
   * @return 求职卡片数据
   */
  SeekerCardDTO getJobCard(Long userId);

  /**
   * 获取所有求职者卡片信息
   *
   * @return 求职者卡片信息列表
   */
   List<SeekerCardDTO> getAllSeekerCards();


  /**
   * 综合筛选求职者卡片信息
   *
   * @param city          城市名称（可选）
   * @param education     学历级别（2-本科，3-硕士，4-博士）（可选）
   * @param salaryMin     最低期望薪资（可选）
   * @param salaryMax     最高期望薪资（可选）
   * @param isInternship  是否实习（0-全职，1-实习）（可选）
   * @param jobStatus     求职状态（0-离校-尽快到岗，1-在校-尽快到岗，2-在校-考虑机会，3-在校-暂不考虑）（可选）
   * @param hasInternship 是否有实习经历（0-无，1-有）（可选）
   * @return 求职者卡片信息列表
   */
  List<SeekerCardDTO> getSeekersByMultipleConditions(
      String city, Integer education, Double salaryMin, Double salaryMax,
      Integer isInternship, Integer jobStatus, Integer hasInternship);
}
