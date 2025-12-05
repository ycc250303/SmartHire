package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.recruitmentService.dto.ApplicationListDTO;
import com.SmartHire.recruitmentService.dto.ApplicationQueryDTO;
import com.SmartHire.recruitmentService.dto.ApplicationStatusUpdateDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 简历管理控制器（HR端） */
@RestController
@RequestMapping("/recruitment/hr")
@Validated
public class HrRecruitmentController {

  @Autowired
  private ApplicationService applicationService;

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
}
