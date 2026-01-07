package com.SmartHire.recruitmentService.service.impl;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.event.OfferSentEvent;
import com.SmartHire.recruitmentService.dto.OfferRequest;
import com.SmartHire.recruitmentService.dto.OfferResponseDTO;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.mapper.OfferMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.recruitmentService.model.Offer;
import com.SmartHire.recruitmentService.service.OfferEventProducer;
import com.SmartHire.recruitmentService.service.OfferService;
import com.SmartHire.common.dto.messageDto.SendMessageCommonDTO;
import com.SmartHire.common.api.MessageApi;
import java.math.BigDecimal;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Offer 服务实现
 */
@Slf4j
@Service
public class OfferServiceImpl implements OfferService {

  @Autowired
  private ApplicationMapper applicationMapper;

  @Autowired
  private OfferMapper offerMapper;

  @Autowired
  private SeekerApi seekerApi;

  @Autowired
  private HrApi hrApi;

  @Autowired
  private MessageApi messageApi;

  @Autowired
  private UserContext userContext;

  @Autowired
  private OfferEventProducer offerEventProducer;

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
    if (!hrApi.isHrAuthorizedForJob(currentHrId, application.getJobId())) {
      throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_HR);
    }

    // 如果 send = true，将 application 状态设置为 已录用 (4)
    if (request.getSend() == null || request.getSend()) {
      application.setStatus((byte) 4);
      application.setUpdatedAt(new Date());
      applicationMapper.updateById(application);

      // 创建并保存 Offer 记录
      Offer offer = new Offer();
      offer.setApplicationId(applicationId);
      offer.setJobSeekerId(application.getJobSeekerId());
      offer.setHrId(currentHrId);
      if (request.getBaseSalary() != null) {
        offer.setBaseSalary(BigDecimal.valueOf(request.getBaseSalary()));
      }
      offer.setStartDate(request.getStartDate());
      offer.setStatus((byte) 0); // 待接受
      Date now = new Date();
      offer.setCreatedAt(now);
      offer.setUpdatedAt(now);
      offerMapper.insert(offer);
      log.info("已保存 Offer 记录: offerId={}, applicationId={}", offer.getId(), applicationId);
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

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void respondToOffer(OfferResponseDTO request) {
    if (request == null || request.getOfferId() == null || request.getResponse() == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Offer offer = offerMapper.selectById(request.getOfferId());
    if (offer == null) {
      throw new BusinessException(ErrorCode.OFFER_NOT_EXIST);
    }

    // 验证当前用户是否有权限响应（必须是该 Offer 的求职者）
    Long currentUserId = userContext.getCurrentUserId();
    Long currentSeekerId = seekerApi.getJobSeekerIdByUserId(currentUserId);
    if (currentSeekerId == null || !currentSeekerId.equals(offer.getJobSeekerId())) {
      log.warn("用户无权响应此 Offer: userId={}, offerId={}", currentUserId, request.getOfferId());
      throw new BusinessException(ErrorCode.PERMISSION_DENIED);
    }

    // 只有待接受(0)状态才能响应
    if (offer.getStatus() != 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Offer 当前状态不可响应");
    }

    Date now = new Date();
    if (request.getResponse() == 1) {
      // 接受
      offer.setStatus((byte) 1);
      offer.setAcceptedAt(now);
    } else if (request.getResponse() == 2) {
      // 拒绝
      offer.setStatus((byte) 2);
      offer.setRejectedAt(now);
    } else {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "非法的响应状态值");
    }

    offer.setUpdatedAt(now);
    int updated = offerMapper.updateById(offer);
    if (updated <= 0) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新 Offer 状态失败");
    }

    log.info("求职者已响应 Offer: offerId={}, response={}", offer.getId(), request.getResponse());

    // 发送消息通知 HR
    sendResponseNotificationToHr(offer, request.getResponse());
  }

  @Override
  public Integer getOfferStatus(Long offerId) {
    Offer offer = offerMapper.selectById(offerId);
    if (offer == null) {
      throw new BusinessException(ErrorCode.OFFER_NOT_EXIST);
    }
    return offer.getStatus() == null ? null : offer.getStatus().intValue();
  }

  /**
   * 向 HR 发送 Offer 响应通知消息
   */
  private void sendResponseNotificationToHr(Offer offer, Integer response) {
    try {
      Long hrUserId = hrApi.getHrUserIdByHrId(offer.getHrId());
      if (hrUserId == null) {
        log.warn("无法找到 HR 对应的用户 ID: hrId={}", offer.getHrId());
        return;
      }

      String action = response == 1 ? "接受" : "拒绝";
      String content = String.format("候选人已%s您的 Offer。Offer ID: %d", action, offer.getId());

      SendMessageCommonDTO messageDTO = new SendMessageCommonDTO();
      messageDTO.setReceiverId(hrUserId);
      messageDTO.setMessageType(1); // 文本消息
      messageDTO.setContent(content);

      messageApi.sendMessage(userContext.getCurrentUserId(), messageDTO);
      log.info("已向 HR 发送 Offer 响应消息通知: hrUserId={}, response={}", hrUserId, action);
    } catch (Exception e) {
      log.error("向 HR 发送 Offer 响应消息通知失败", e);
    }
  }
}
