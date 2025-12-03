package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/seeker/recruitment")
public class SeekerRecruitmentController {
  @Autowired private ApplicationService applicationService;

  @PostMapping("/submit-resume")
  @Operation(
      summary = "求职者投递简历",
      description = "求职者投递简历到指定职位。如果提供resumeId则投递附件简历，如果不提供resumeId则投递在线简历")
  public Result<?> submitResume(@Valid @RequestBody SubmitResumeDTO request) {
    applicationService.submitResume(request);
    return Result.success("投递简历成功");
  }
}
