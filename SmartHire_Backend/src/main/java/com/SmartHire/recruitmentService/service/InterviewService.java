package com.SmartHire.recruitmentService.service;

import com.SmartHire.recruitmentService.dto.InterviewResponseDTO;
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

  /**
   * 响应面试邀请（求职者调用）
   *
   * @param request 响应请求
   */
  void respondToInterview(InterviewResponseDTO request);

  /**
   * 获取面试状态
   *
   * @param interviewId 面试ID
   * @return 面试状态：0-待确认 1-已确认 2-已完成 3-已取消
   */
  Integer getInterviewStatus(Long interviewId);

  /**
   * 根据消息ID获取面试ID
   *
   * @param messageId 消息ID
   * @return 面试ID
   */
  Long getInterviewIdByMessageId(Long messageId);
}
