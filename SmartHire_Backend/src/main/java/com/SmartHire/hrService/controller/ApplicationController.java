package com.SmartHire.hrService.controller;

import com.SmartHire.hrService.dto.ApplicationCommentUpdateDTO;
import com.SmartHire.hrService.dto.ApplicationListDTO;
import com.SmartHire.hrService.dto.ApplicationQueryDTO;
import com.SmartHire.hrService.dto.ApplicationStatusUpdateDTO;
import com.SmartHire.hrService.service.ApplicationService;
import com.SmartHire.shared.annotation.RequireHr;
import com.SmartHire.shared.entity.Result;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 简历管理控制器
 */
@RestController
@RequestMapping("/hr/application")
@Validated
@RequireHr
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 投递列表查询
     */
    @GetMapping
    @Operation(summary = "简历列表查看", description = "按岗位/状态/关键词分页查询投递记录")
    public Result<Page<ApplicationListDTO>> getApplicationList(@Valid ApplicationQueryDTO queryDTO) {
        Page<ApplicationListDTO> page = applicationService.getApplicationList(queryDTO);
        return Result.success("查询成功", page);
    }

    /**
     * 投递详情
     */
    @GetMapping("/{applicationId}")
    @Operation(summary = "简历详情", description = "根据投递ID获取详情")
    public Result<ApplicationListDTO> getApplicationDetail(
            @PathVariable @Min(value = 1, message = "投递ID非法") Long applicationId) {
        ApplicationListDTO detail = applicationService.getApplicationDetail(applicationId);
        return Result.success("查询成功", detail);
    }

    /**
     * 更新投递状态
     */
    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "简历状态跟踪", description = "更新投递状态（未读/已读/通过/拒绝等）")
    public Result<?> updateApplicationStatus(
            @PathVariable @Min(value = 1, message = "投递ID非法") Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateDTO updateDTO) {
        applicationService.updateApplicationStatus(applicationId, updateDTO.getStatus());
        return Result.success("状态更新成功");
    }

    /**
     * 更新HR备注
     * 注意：由于数据库表中没有hr_comment字段，此功能暂时禁用
     */
    @PatchMapping("/{applicationId}/comment")
    @Operation(summary = "简历备注", description = "记录HR备注与查看时间（功能暂不可用）")
    public Result<?> updateApplicationComment(
            @PathVariable @Min(value = 1, message = "投递ID非法") Long applicationId,
            @Valid @RequestBody ApplicationCommentUpdateDTO updateDTO) {
        // 由于数据库表中没有hr_comment字段，此功能暂时返回错误
        return Result.error(ErrorCode.SYSTEM_ERROR, "备注功能暂不可用，数据库表中缺少相应字段");
    }
}

