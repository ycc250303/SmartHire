package com.SmartHire.common.api;

import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.Resume;

/** 求职者服务API接口 用于模块间通信，避免直接访问数据库 */
public interface SeekerApi {

  /**
   * 根据用户ID获取求职者ID
   *
   * @param userId 用户ID
   * @return 求职者ID
   */
  Long getJobSeekerIdByUserId(Long userId);

  /**
   * 根据求职者ID获取求职者信息
   *
   * @param jobSeekerId 求职者ID
   * @return 求职者信息
   */
  JobSeeker getJobSeekerById(Long jobSeekerId);

  /**
   * 根据简历ID获取简历信息
   *
   * @param resumeId 简历ID
   * @return 简历信息
   */
  Resume getResumeById(Long resumeId);

  /**
   * 验证简历是否属于指定求职者
   *
   * @param resumeId    简历ID
   * @param jobSeekerId 求职者ID
   * @return 是否属于
   */
  boolean validateResumeOwnership(Long resumeId, Long jobSeekerId);

  /**
   * 根据用户ID删除求职者信息（级联删除所有相关数据） 用于管理员删除用户时调用
   *
   * @param userId 用户ID
   */
  void deleteJobSeekerByUserId(Long userId);

  /**
   * 根据用户ID获取求职者卡片信息
   *
   * @param userId 用户ID
   * @return 求职者卡片信息，如果用户不是求职者或求职者不存在返回null
   */
  SeekerCardDTO getSeekerCard(Long userId);

}
