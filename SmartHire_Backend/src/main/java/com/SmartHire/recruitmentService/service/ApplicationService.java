package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.model.Application;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 投递/推荐记录表 服务类
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
public interface ApplicationService extends IService<Application> {

  /**
   * 投递简历（求职者端）
   *
   * @param request 投递简历请求
   */
  void submitResume(SubmitResumeDTO request);

  /**
   * 查询当前HR的投递列表（HR端）
   *
   * @param queryDTO 查询参数
   * @return 分页结果
   */
  Page<ApplicationListDTO> getApplicationList(ApplicationQueryDTO queryDTO);

  /**
   * 获取投递详情（HR端）
   *
   * @param applicationId 投递ID
   * @return 投递详情
   */
  ApplicationListDTO getApplicationDetail(Long applicationId);

  /**
   * 更新投递状态（HR端）
   *
   * @param applicationId 投递ID
   * @param status        新状态
   */
  void updateApplicationStatus(Long applicationId, Byte status);
}
