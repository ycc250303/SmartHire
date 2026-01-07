package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.api.AdminApi;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.common.dto.adminDto.ReportSubmitDTO;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.recruitmentService.dto.InternJobRecommendationsDTO;
import com.SmartHire.recruitmentService.dto.SeekerApplicationListDTO;
import com.SmartHire.recruitmentService.dto.SeekerJobPositionDTO;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.SeekerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 招聘服务求职者端招聘控制器
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/recruitment/seeker")
@RequireUserType(UserType.SEEKER)
@Validated
public class SeekerRecruitmentController {
    @Autowired
    private SeekerApplicationService seekerApplicationService;

    @Autowired
    private AdminApi adminApi;

    @Autowired
    private UserContext userContext;

    @PostMapping("/submit-resume")
    @Operation(summary = "求职者投递简历", description = "求职者投递简历到指定职位。如果提供resumeId则投递附件简历，如果不提供resumeId则投递在线简历")
    public Result<?> submitResume(@Valid @RequestBody SubmitResumeDTO request) {
        Long applicationId = seekerApplicationService.submitResume(request);
        return Result.success("投递简历成功", applicationId);
    }

    @GetMapping("/job-card/{jobId}")
    @Operation(summary = "获取岗位卡片", description = "根据岗位ID获取岗位卡片信息（包含岗位、公司、HR信息）")
    public Result<JobCardDTO> getJobCard(
            @PathVariable @NotNull(message = "岗位ID不能为空") @Min(value = 1, message = "岗位ID必须为正整数") Long jobId) {
        JobCardDTO jobCard = seekerApplicationService.getJobCard(jobId);
        return Result.success("获取岗位卡片成功", jobCard);
    }

    @GetMapping("/job-list")
    @Operation(summary = "获取投递过的列表", description = "获取投递过的职位列表")
    public Result<SeekerApplicationListDTO> getSeekerApplicationList(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        SeekerApplicationListDTO result = seekerApplicationService.getSeekerApplicationList(page, size);
        return Result.success("获取投递过的列表成功", result);
    }

    @GetMapping("/job-recommendations/intern")
    @Operation(summary = "获取实习岗位推荐", description = "获取用户首页推荐的实习岗位列表，优先使用简历/求职者信息进行关键词匹配计算得分（向量搜索未就绪）")
    public Result<InternJobRecommendationsDTO> getInternJobRecommendations() {
        InternJobRecommendationsDTO resp = seekerApplicationService.getInternJobRecommendations();
        return Result.success("获取岗位列表成功", resp);
    }

    @GetMapping("/job-recommendations/full-time")
    @Operation(summary = "获取实习岗位推荐", description = "获取用户首页推荐的实习岗位列表，优先使用简历/求职者信息进行关键词匹配计算得分（向量搜索未就绪）")
    public Result<InternJobRecommendationsDTO> getFullTimeJobRecommendations() {
        InternJobRecommendationsDTO resp = seekerApplicationService.getInternJobRecommendations();
        return Result.success("获取岗位列表成功", resp);
    }

    @GetMapping("/job-position/{jobId}")
    @Operation(summary = "获取面向求职者的岗位详情", description = "返回岗位详情（包含公司、HR、申请状态等），供求职者端展示")
    public Result<SeekerJobPositionDTO> getJobPosition(@PathVariable @NotNull Long jobId) {
        SeekerJobPositionDTO resp = seekerApplicationService.getJobPosition(jobId);
        return Result.success("查询成功", resp);
    }

    @PostMapping("/report")
    @Operation(summary = "举报职位", description = "举报职位")
    public Result<?> reportJob(@Valid @RequestBody ReportSubmitDTO request) {
        request.setTargetType(2); // 职位
        adminApi.submitReport(request, userContext.getCurrentUserId());
        return Result.success("举报成功");
    }
}
