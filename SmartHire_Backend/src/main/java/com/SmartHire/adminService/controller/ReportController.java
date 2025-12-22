package com.SmartHire.adminService.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.SmartHire.adminService.dto.ReportDetailDTO;
import com.SmartHire.adminService.dto.ReportQueryDTO;
import com.SmartHire.adminService.dto.ReportHandleDTO;
import com.SmartHire.adminService.service.ReportService;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.common.exception.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 举报管理控制器
 */
@RestController
@RequestMapping("/admin/reports")
@RequireUserType(UserType.ADMIN)  // 只有管理员可以访问举报管理接口
@Validated
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 分页查询举报列表
     */
    @GetMapping
    public Result<Page<ReportDetailDTO>> getReportList(ReportQueryDTO queryDTO) {
        Page<ReportDetailDTO> result = reportService.getReportList(queryDTO);

        return Result.success(result);
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    public Result<ReportDetailDTO> getReportDetail(
            @PathVariable @NotNull(message = "举报ID不能为空") Long id) {
        ReportDetailDTO detail = reportService.getReportDetail(id);
        if (detail == null) {
            return Result.error(ErrorCode.REPORT_NOT_FOUND);
        }
        return Result.success(detail);
    }

    /**
     * 处理举报
     */
    @PutMapping("/{id}/handle")
    public Result<Boolean> handleReport(
            @PathVariable @NotNull(message = "举报ID不能为空") Long id,
            @Valid @RequestBody ReportHandleDTO handleDTO,
            @RequestParam @NotNull Long operatorId) {

        boolean success = reportService.handleReport(id, handleDTO, operatorId);
        if (success) {
            return Result.success("举报处理成功", true);
        } else {
            return Result.error(ErrorCode.REPORT_HANDLE_FAILED);
        }
    }

    /**
     * 获取举报统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getReportStats() {
        Map<String, Object> stats = reportService.getReportStats();
        return Result.success(stats);
    }

    /**
     * 下载证据图片
     */
    @GetMapping("/{id}/evidence")
    public Result<String> getEvidenceImage(
            @PathVariable @NotNull(message = "举报ID不能为空") Long id) {
        String evidenceImage = reportService.getEvidenceImage(id);
        if (evidenceImage == null || evidenceImage.isEmpty()) {
            return Result.error(ErrorCode.REPORT_EVIDENCE_NOT_FOUND);
        }
        return Result.success(evidenceImage);
    }
}