package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.SeekerDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 求职者信息表 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface JobSeekerService extends IService<JobSeeker> {
  /**
   * 注册求职者
   *
   * @param request 注册求职者信息
   */
  void registerSeeker(SeekerDTO request);

  /**
   * 获取求职者信息
   *
   * @return 求职者信息
   */
  JobSeeker getSeekerInfo();

  /**
   * 获取求职者ID
   *
   * @return 求职者ID
   */
  Long getJobSeekerId();

  /**
   * 更新求职者信息
   *
   * @param request 更新求职者信息
   */
  void updateSeekerInfo(SeekerDTO request);

  /** 删除求职者信息（级联删除所有相关数据） 删除当前登录用户的求职者信息 */
  void deleteJobSeeker();

  /**
   * 根据用户ID删除求职者信息（级联删除所有相关数据） 用于管理员删除用户时调用
   *
   * @param userId 用户ID
   */
  void deleteJobSeekerByUserId(Long userId);
}
