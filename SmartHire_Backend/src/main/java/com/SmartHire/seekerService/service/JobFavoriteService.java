package com.SmartHire.seekerService.service;

import com.SmartHire.seekerService.dto.JobFavoriteDTO;
import com.SmartHire.seekerService.model.JobFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 职位收藏表 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
public interface JobFavoriteService extends IService<JobFavorite> {
  /**
   * 收藏岗位
   *
   * @param jobId 岗位ID
   */
  void addJobFavorite(Long jobId);

  /**
   * 取消收藏岗位
   *
   * @param jobId 岗位ID
   */
  void removeJobFavorite(Long jobId);

  /**
   * 获取收藏岗位列表
   *
   * @return 收藏岗位列表（包含岗位信息）
   */
  List<JobFavoriteDTO> getJobFavoriteList();
}
