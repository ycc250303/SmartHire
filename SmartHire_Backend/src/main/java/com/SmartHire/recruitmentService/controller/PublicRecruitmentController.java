package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.recruitmentService.service.InterviewService;
import com.SmartHire.recruitmentService.service.SeekerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 招聘服务公共招聘控制器
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/recruitment/public")
@Validated
public class PublicRecruitmentController {
    @Autowired
    private SeekerApplicationService seekerApplicationService;

    @Autowired
    private InterviewService interviewService;

    @GetMapping("/application/exists")
    @Operation(summary = "检查是否存在投递/推荐记录", description = "检查是否存在投递/推荐记录")
    public Result<Boolean> checkApplicationExists(
            @RequestParam @NotNull(message = "求职者ID不能为空") Long seekerId,
            @RequestParam @NotNull(message = "岗位ID不能为空") Long jobId) {

        boolean exists = seekerApplicationService.existsBySeekerIdAndJobId(seekerId, jobId);
        return Result.success("查询成功", exists);
    }

    @GetMapping("/interview/status")
    @Operation(summary = "获取面试状态", description = "根据面试ID获取当前面试状态（0-待确认 1-已确认 2-已完成 3-已取消）")
    public Result<Integer> getInterviewStatus(
            @RequestParam @NotNull(message = "面试ID不能为空") Long interviewId) {
        Integer status = interviewService.getInterviewStatus(interviewId);
        return Result.success("查询成功", status);
    }
}
