package com.SmartHire.recruitmentService.controller;


import com.SmartHire.common.entity.Result;
import com.SmartHire.recruitmentService.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recruitment/public")
@Validated
public class PublicRecruitmentController {
    @Autowired
    @Qualifier("applicationServiceImpl")
    private ApplicationService applicationService;

    @GetMapping("/application/exists")
    @Operation(summary = "检查是否存在投递/推荐记录", description = "检查是否存在投递/推荐记录")
    public Result<Boolean> checkApplicationExists(
            @RequestParam @NotNull(message = "求职者ID不能为空") Long seekerId,
            @RequestParam @NotNull(message = "岗位ID不能为空") Long jobId) {
        boolean exists = applicationService.existsBySeekerIdAndJobId(seekerId, jobId);
        return Result.success("查询成功", exists);
    }
}
