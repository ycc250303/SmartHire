package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.ApplicationStatusUpdateDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.common.dto.seekerDto.SeekerCardDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.SmartHire.recruitmentService.dto.RecommendRequest;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserType;

/** 简历管理控制器（HR端） */
@RestController
@RequestMapping("/recruitment/hr")
@Validated
public class HrRecruitmentController {

  @Autowired
  @Qualifier("applicationServiceImpl")
  private ApplicationService applicationService;

  @Autowired
  private com.SmartHire.recruitmentService.service.InterviewService interviewService;

  @Autowired
  private SeekerApi seekerApi;

  /** 投递列表查询 */
  @GetMapping("/application")
  @Operation(summary = "简历列表查看", description = "按岗位/状态/关键词分页查询投递记录")
  public Result<Page<ApplicationListDTO>> getApplicationList(@Valid ApplicationQueryDTO queryDTO) {
    Page<ApplicationListDTO> page = applicationService.getApplicationList(queryDTO);
    return Result.success("查询成功", page);
  }

  /** 投递详情 */
  @GetMapping("/{applicationId}")
  @Operation(summary = "简历详情", description = "根据投递ID获取详情")
  public Result<ApplicationListDTO> getApplicationDetail(
      @PathVariable @Min(value = 1, message = "投递ID非法") Long applicationId) {
    ApplicationListDTO detail = applicationService.getApplicationDetail(applicationId);
    return Result.success("查询成功", detail);
  }

  /** 更新投递状态 */
  @PatchMapping("/{applicationId}/status")
  @Operation(summary = "简历状态跟踪", description = "更新投递状态（未读/已读/通过/拒绝等）")
  public Result<?> updateApplicationStatus(
      @PathVariable @Min(value = 1, message = "投递ID非法") Long applicationId,
      @Valid @RequestBody ApplicationStatusUpdateDTO updateDTO) {
    applicationService.updateApplicationStatus(applicationId, updateDTO.getStatus());
    return Result.success("状态更新成功");
  }

  @GetMapping("/seeker-card")
  @Operation(summary = "获取求职者卡片", description = "根据用户ID获取求职者卡片信息（包含用户名、年龄、学历、专业等）")
  public Result<SeekerCardDTO> getSeekerCard(
      @RequestParam("userId") @NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须为正整数") Long userId) {
    // SeekerApi.getSeekerCard() 会抛出 USER_NOT_SEEKER 异常，或返回 null（表示求职者不存在）
    SeekerCardDTO seekerCard = seekerApi.getSeekerCard(userId);
    if (seekerCard == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }
    return Result.success("获取求职者卡片成功", seekerCard);
  }

  /**
   * HR 推荐岗位给求职者（创建推荐记录）
   *
   * @param req 推荐请求
   */
  @PostMapping("/recommend")
  @RequireUserType(UserType.HR)
  @Operation(summary = "HR 推荐岗位", description = "公司 HR 推荐岗位给指定求职者（创建推荐记录）")
  public Result<?> recommend(@Valid @RequestBody RecommendRequest req) {
    Long applicationId = applicationService.recommend(req);
    return Result.success("推荐成功", applicationId);
  }

  @Autowired
  private com.SmartHire.recruitmentService.service.OfferService offerService;

  /**
   * HR 发 Offer（最小可行：发送即置为已录用）
   *
   * @param req Offer 请求
   */
  @PostMapping("/offers")
  @RequireUserType(UserType.HR)
  @Operation(summary = "HR 发 Offer", description = "HR 向求职者发 Offer（发送则将 application.status 置为已录用）")
  public Result<?> sendOffer(@Valid @RequestBody com.SmartHire.recruitmentService.dto.OfferRequest req) {
    Long applicationId = offerService.sendOffer(req);
    return Result.success("Offer 处理成功", applicationId);
  }

  /**
   * HR 安排面试（创建面试记录并通知求职者）
   *
   * @param req 面试安排请求
   */
  @PostMapping("/interviews")
  @RequireUserType(UserType.HR)
  @Operation(summary = "HR 安排面试", description = "HR 为指定投递安排面试并通知候选人")
  public Result<?> scheduleInterview(
      @Valid @RequestBody com.SmartHire.recruitmentService.dto.InterviewScheduleRequest req) {
    Long interviewId = interviewService.scheduleInterview(req);
    return Result.success("面试安排成功", interviewId);
  }

  /**
   * HR 拒绝候选人（单条）
   *
   * @param applicationId 投递ID
   * @param req           拒绝请求体
   */
  @PatchMapping("/application/{applicationId}/reject")
  @RequireUserType(UserType.HR)
  @Operation(summary = "HR 拒绝候选人", description = "HR 对指定投递执行拒绝操作（记录原因并通知候选人）")
  public Result<?> rejectApplication(
      @PathVariable Long applicationId, @Valid @RequestBody com.SmartHire.recruitmentService.dto.RejectRequest req) {
    applicationService.rejectApplication(applicationId, req.getReason(), req.getSendNotification(),
        req.getTemplateId());
    return Result.success("拒绝成功", applicationId);
  }
}
