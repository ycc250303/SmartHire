package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.context.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.event.OfferSentEvent;
import com.SmartHire.recruitmentService.dto.OfferRequest;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.service.OfferEventProducer;
import com.SmartHire.recruitmentService.service.OfferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Offer 服务实现（不建表版） 将应用状态置为已录用并发布事件
 */
@Slf4j
@Service
public class OfferServiceImpl implements OfferService {

  @Autowired private ApplicationMapper applicationMapper;

  @Autowired private SeekerApi seekerApi;

  @Autowired private HrApi hrApi;

  @Autowired private UserContext userContext;

  @Autowired private OfferEventProducer offerEventProducer;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long sendOffer(OfferRequest request) {
    if (request == null || request.getApplicationId() == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Long applicationId = request.getApplicationId();

    Application application = applicationMapper.selectById(applicationId);
    if (application == null) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_EXIST);
    }

    // 权限校验：当前用户应为发布该岗位的 HR
    Long currentUserId = userContext.getCurrentUserId();
    Long currentHrId = hrApi.getHrIdByUserId(currentUserId);
    if (currentHrId == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }
    Long jobHrId = hrApi.getHrIdByJobId(application.getJobId());
    if (jobHrId == null || !jobHrId.equals(currentHrId)) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
    }

    // 如果 send = true，将 application 状态设置为 已录用 (4)
    if (request.getSend() == null || request.getSend()) {
      application.setStatus((byte) 4);
      applicationMapper.updateById(application);
    }

    // 发布事件，用于消息服务异步发送通知
    Long jobSeekerId = application.getJobSeekerId();
    Long seekerUserId = seekerApi.getJobSeekerById(jobSeekerId).getUserId();
    Long hrUserId = hrApi.getHrUserIdByHrId(currentHrId);

    OfferSentEvent event = new OfferSentEvent();
    event.setApplicationId(applicationId);
    event.setSeekerUserId(seekerUserId);
    event.setHrUserId(hrUserId);
    event.setTitle(request.getTitle());
    event.setBaseSalary(request.getBaseSalary());
    event.setBonus(request.getBonus());
    event.setStartDate(request.getStartDate());
    event.setEmploymentType(request.getEmploymentType());
    event.setNote(request.getNote());

    offerEventProducer.publishOfferSent(event);
    log.info("Offer 已发布事件: applicationId={}, seekerUserId={}", applicationId, seekerUserId);

    return applicationId;
  }
}


