package com.SmartHire.recruitmentService.controller;

import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.shared.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 投递/推荐岗位服务统一控制器
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/recruitment")
public class RecruitmentController {
    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/submit-resume")
    @Operation(summary = "求职者投递简历", description = "求职者投递简历到指定职位")
    public Result<?> submitResume(@Valid @RequestBody SubmitResumeDTO request) {
        applicationService.submitResume(request);
        return Result.success("投递简历成功");
    }

}
