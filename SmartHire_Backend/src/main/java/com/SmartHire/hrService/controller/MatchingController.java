package com.SmartHire.hrService.controller;

import com.SmartHire.hrService.dto.ApplicationListDTO;
import com.SmartHire.hrService.service.MatchingService;
import com.SmartHire.shared.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 匹配功能控制器
 */
@RestController
@RequestMapping("/hr/matching")
@Validated
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    /**
     * 对指定岗位执行关键词匹配并返回排序结果
     */
    @GetMapping("/job/{jobId}")
    @Operation(summary = "岗位匹配结果", description = "计算岗位与投递简历的关键词匹配得分并返回排序列表")
    public Result<List<ApplicationListDTO>> matchApplicationsForJob(
            @PathVariable @Min(value = 1, message = "岗位ID非法") Long jobId) {
        List<ApplicationListDTO> results = matchingService.matchApplicationsForJob(jobId);
        return Result.success("匹配完成", results);
    }
}

