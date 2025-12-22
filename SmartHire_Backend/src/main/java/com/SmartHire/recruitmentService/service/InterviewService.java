package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.InterviewScheduleRequest;

/**
 * 面试服务接口
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
public interface InterviewService {
  /**
   * 安排面试（HR 调用） 创建 interview 记录、更新 application 状态并发布事件
   *
   * @param request 面试安排请求
   * @return interviewId
   */
  Long scheduleInterview(InterviewScheduleRequest request);
}


