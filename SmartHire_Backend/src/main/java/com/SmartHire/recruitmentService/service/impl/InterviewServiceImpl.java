package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.MessageApi;
import com.SmartHire.common.dto.messageDto.MessageCommonDTO;
import com.SmartHire.common.dto.messageDto.SendMessageCommonDTO;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.InterviewResponseDTO;
import com.SmartHire.recruitmentService.dto.InterviewScheduleRequest;
import com.SmartHire.recruitmentService.mapper.InterviewMapper;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Interview;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.InterviewService;
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
  private MessageApi messageApi;

  @Autowired
  private UserContext userContext;

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

    // 构建并发送消息通知求职者
    String content = String.format(
        "您好，您有一场新的面试安排：时间：%s，地点：%s，面试官：%s。请按时参加。如有问题请联系HR。",
        request.getInterviewTime() != null ? request.getInterviewTime().toString() : "未指定",
        request.getLocation() != null ? request.getLocation() : "未指定",
        request.getInterviewer() != null ? request.getInterviewer() : "未指定");
    if (request.getNote() != null && !request.getNote().isEmpty()) {
      content = request.getNote();
    }

    SendMessageCommonDTO messageDTO = new SendMessageCommonDTO();
    messageDTO.setReceiverId(seekerUserId);
    messageDTO.setMessageType(8); // 面试邀请
    messageDTO.setContent(content);

    MessageCommonDTO messageResult = messageApi.sendMessage(hrUserId, messageDTO);
    if (messageResult != null && messageResult.getId() != null) {
      interview.setMessageId(messageResult.getId());
      this.updateById(interview);
      log.info("面试安排消息已发送, messageId={}, interviewId={}", messageResult.getId(), interview.getId());
    } else {
      log.warn("面试安排消息发送成功但未返回消息ID, interviewId={}", interview.getId());
    }

    return interview.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void respondToInterview(InterviewResponseDTO request) {
    if (request == null || request.getInterviewId() == null || request.getResponse() == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Interview interview = this.getById(request.getInterviewId());
    if (interview == null) {
      throw new BusinessException(ErrorCode.INTERVIEW_NOT_EXIST);
    }

    // 验证当前用户是否有权限响应（必须是该投递的求职者）
    Application application = applicationMapper.selectById(interview.getApplicationId());
    if (application == null) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
    }

    Long currentUserId = userContext.getCurrentUserId();
    Long currentSeekerId = seekerApi.getJobSeekerIdByUserId(currentUserId);
    if (currentSeekerId == null || !currentSeekerId.equals(application.getJobSeekerId())) {
      log.warn("用户无权响应此面试邀请: userId={}, interviewId={}", currentUserId, request.getInterviewId());
      throw new BusinessException(ErrorCode.PERMISSION_DENIED);
    }

    // 检查当前面试状态，只有待确认(0)状态才能响应
    if (interview.getStatus() != 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "面试当前状态不可响应");
    }

    if (request.getResponse() == 1) {
      // 接受
      interview.setStatus((byte) 1); // 已确认
    } else if (request.getResponse() == 2) {
      // 拒绝
      interview.setStatus((byte) 3); // 已取消/已拒绝
    } else {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "非法的响应状态值");
    }

    interview.setUpdatedAt(new Date());
    boolean updated = this.updateById(interview);
    if (!updated) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新面试状态失败");
    }

    log.info("求职者已响应面试邀请: interviewId={}, response={}", interview.getId(), request.getResponse());

    // 发送消息通知 HR
    sendResponseNotificationToHr(application, request.getResponse());
  }

  /**
   * 向 HR 发送响应通知消息
   *
   * @param application 投递记录
   * @param response    响应结果 (1-接受, 2-拒绝)
   */
  private void sendResponseNotificationToHr(Application application, Integer response) {
    try {
      Long hrId = hrApi.getHrIdByJobId(application.getJobId());
      if (hrId == null) {
        log.warn("无法找到岗位的对应HR: jobId={}", application.getJobId());
        return;
      }
      Long hrUserId = hrApi.getHrUserIdByHrId(hrId);
      if (hrUserId == null) {
        log.warn("无法找到HR对应的用户ID: hrId={}", hrId);
        return;
      }

      String action = response == 1 ? "接受" : "拒绝";
      String content = String.format("候选人已%s您的面试邀请。面试记录ID: %d", action, application.getId());

      SendMessageCommonDTO messageDTO = new SendMessageCommonDTO();
      messageDTO.setReceiverId(hrUserId);
      messageDTO.setMessageType(1); // 文本消息
      messageDTO.setContent(content);

      messageApi.sendMessage(userContext.getCurrentUserId(), messageDTO);
      log.info("已向HR发送面试响应消息通知: hrUserId={}, response={}", hrUserId, action);
    } catch (Exception e) {
      log.error("向HR发送面试响应消息通知失败", e);
      // 通知的失败不应影响主业务流程的回滚
    }
  }

  @Override
  public Integer getInterviewStatus(Long interviewId) {
    Interview interview = this.getById(interviewId);
    if (interview == null) {
      throw new BusinessException(ErrorCode.INTERVIEW_NOT_EXIST);
    }
    return interview.getStatus() == null ? null : interview.getStatus().intValue();
  }

  @Override
  public Long getInterviewIdByMessageId(Long messageId) {
    if (messageId == null) {
      return null;
    }
    Interview interview = this.lambdaQuery()
        .eq(Interview::getMessageId, messageId)
        .one();
    return interview != null ? interview.getId() : null;
  }
}
