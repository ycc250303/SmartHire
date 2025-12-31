package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.InterviewScheduleRequest;
import com.SmartHire.recruitmentService.mapper.InterviewMapper;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Interview;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.InterviewService;
import com.SmartHire.recruitmentService.service.InterviewEventProducer;
import com.SmartHire.common.event.InterviewScheduledEvent;
import com.SmartHire.common.auth.UserContext;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interview 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-12-21
 */
@Slf4j
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview>
    implements InterviewService {

  @Autowired
  private ApplicationMapper applicationMapper;

  @Autowired
  private SeekerApi seekerApi;

  @Autowired
  private HrApi hrApi;

  @Autowired
  private UserContext userContext;

  @Autowired
  private InterviewEventProducer interviewEventProducer;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long scheduleInterview(InterviewScheduleRequest request) {
    if (request == null || request.getApplicationId() == null || request.getInterviewTime() == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Long applicationId = request.getApplicationId();

    // 验证 application 存在
    Application application = applicationMapper.selectById(applicationId);
    if (application == null) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
    }

    // 校验当前用户是该岗位的 HR
    Long currentUserId = userContext.getCurrentUserId();
    Long currentHrId = hrApi.getHrIdByUserId(currentUserId);
    if (currentHrId == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }
    if (!hrApi.isHrAuthorizedForJob(currentHrId, application.getJobId())) {
      throw new BusinessException(ErrorCode.PERMISSION_DENIED);
    }

    // 创建 interview 记录
    Interview interview = new Interview();
    interview.setApplicationId(applicationId);
    interview.setInterviewTime(request.getInterviewTime());
    interview.setDuration(request.getDuration());
    interview.setInterviewRound(request.getInterviewRound());
    interview.setInterviewType(request.getInterviewType() == null ? null : request.getInterviewType().byteValue());
    interview.setLocation(request.getLocation());
    interview.setMeetingLink(request.getMeetingLink());
    interview.setInterviewer(request.getInterviewer());
    interview.setStatus((byte) 0); // 待确认
    Date now = new Date();
    interview.setCreatedAt(now);
    interview.setUpdatedAt(now);

    boolean saved = this.save(interview);
    if (!saved) {
      log.error("保存面试记录失败, applicationId={}, interviewTime={}", applicationId, request.getInterviewTime());
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    // 更新 application 状态为 待面试 (2)
    application.setStatus((byte) 2);
    application.setUpdatedAt(now);
    applicationMapper.updateById(application);

    // 获取 seeker 的用户ID（用于通知）
    Long jobSeekerId = application.getJobSeekerId();
    Long seekerUserId = seekerApi.getJobSeekerById(jobSeekerId).getUserId();

    // 获取 HR 用户 ID（发送者）
    Long hrUserId = hrApi.getHrUserIdByHrId(currentHrId);

    // 发布 InterviewScheduledEvent 到队列，通知消息服务异步发送通知
    InterviewScheduledEvent event = new InterviewScheduledEvent();
    event.setInterviewId(interview.getId());
    event.setApplicationId(applicationId);
    event.setSeekerUserId(seekerUserId);
    event.setHrUserId(hrUserId);
    event.setInterviewTime(request.getInterviewTime());
    event.setMeetingLink(request.getMeetingLink());
    event.setLocation(request.getLocation());
    event.setInterviewer(request.getInterviewer());
    event.setNote(request.getNote());

    interviewEventProducer.publishInterviewScheduled(event);
    log.info("面试安排已发布事件: interviewId={}, applicationId={}", interview.getId(), applicationId);

    return interview.getId();
  }
}
