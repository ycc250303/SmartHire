package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投递岗位控制器
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/recruitment/seeker")
@Validated
public class SeekerRecruitmentController {
  @Autowired
  @Qualifier("applicationServiceImpl")
  private ApplicationService applicationService;

  @Autowired private HrApi hrApi;

  @PostMapping("/submit-resume")
  @Operation(
      summary = "求职者投递简历",
      description = "求职者投递简历到指定职位。如果提供resumeId则投递附件简历，如果不提供resumeId则投递在线简历")
  public Result<?> submitResume(@Valid @RequestBody SubmitResumeDTO request) {
    applicationService.submitResume(request);
    return Result.success("投递简历成功");
  }

  @GetMapping("/job-card/{jobId}")
  @Operation(summary = "获取岗位卡片", description = "根据岗位ID获取岗位卡片信息（包含岗位、公司、HR信息）")
  public Result<JobCardDTO> getJobCard(
      @PathVariable @NotNull(message = "岗位ID不能为空") @Min(value = 1, message = "岗位ID必须为正整数")
          Long jobId) {
    JobCardDTO jobCard = hrApi.getJobCardByJobId(jobId);
    if (jobCard == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }
    return Result.success("获取岗位卡片成功", jobCard);
  }
}
