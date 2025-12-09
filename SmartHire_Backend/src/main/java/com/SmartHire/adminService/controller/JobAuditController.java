package com.SmartHire.adminService.controller;

import com.SmartHire.adminService.dto.JobAuditDTO;
import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.SmartHire.adminService.dto.JobAuditQueryDTO;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.SmartHire.adminService.service.JobAuditService;
import com.SmartHire.common.entity.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 职位审核控制器
 * 参考用户封禁功能的实现方式
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/review")
@Validated
public class JobAuditController {

    @Autowired
    private JobAuditService jobAuditService;

    /**
     * 获取审核列表（完全支持前端功能）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/jobs")
    public Result<Page<JobAuditListDTO>> getAuditList(JobAuditQueryDTO queryDTO) {
        Page<JobAuditListDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<JobAuditListDTO> result = jobAuditService.getAuditList(page, queryDTO);
        return Result.success(result);
    }

    /**
     * 通过审核
     *
     * @param jobId 职位ID
     * @return 操作结果
     */
    @PostMapping("/jobs/{jobId}/approve")
    public Result<?> approveJob(@PathVariable Long jobId) {
        // 从SecurityContext获取当前管理员信息
        // 这里暂时使用模拟的管理员信息，实际应该从Spring Security上下文获取
        Long auditorId = 1L; // 模拟管理员ID
        String auditorName = "系统管理员"; // 模拟管理员姓名

        jobAuditService.approveJob(jobId, auditorId, auditorName);
        return Result.success("审核通过");
    }

    /**
     * 拒绝审核
     *
     * @param jobId 职位ID
     * @param auditDTO 审核DTO（包含拒绝原因）
     * @return 操作结果
     */
    @PostMapping("/jobs/{jobId}/reject")
    public Result<?> rejectJob(@PathVariable Long jobId,
                             @Valid @RequestBody JobAuditDTO auditDTO) {
        // 从SecurityContext获取当前管理员信息
        Long auditorId = 1L; // 模拟管理员ID
        String auditorName = "系统管理员"; // 模拟管理员姓名

        jobAuditService.rejectJob(jobId, auditDTO.getReason(), auditorId, auditorName);
        return Result.success("已拒绝该职位");
    }

    /**
     * 要求修改
     *
     * @param jobId 职位ID
     * @param auditDTO 审核DTO（包含修改意见）
     * @return 操作结果
     */
    @PostMapping("/jobs/{jobId}/modify")
    public Result<?> modifyJob(@PathVariable Long jobId,
                             @Valid @RequestBody JobAuditDTO auditDTO) {
        // 从SecurityContext获取当前管理员信息
        Long auditorId = 1L; // 模拟管理员ID
        String auditorName = "系统管理员"; // 模拟管理员姓名

        jobAuditService.modifyJob(jobId, auditDTO.getReason(), auditorId, auditorName);
        return Result.success("已要求修改该职位");
    }

    /**
     * 获取审核详情
     *
     * @param jobId 职位ID
     * @return 审核详情
     */
    @GetMapping("/jobs/{jobId}")
    public Result<JobAuditRecord> getAuditDetail(@PathVariable Long jobId) {
        JobAuditRecord detail = jobAuditService.getAuditDetail(jobId);
        return Result.success(detail);
    }

    /**
     * 按状态统计数量
     *
     * @param status 审核状态
     * @return 数量
     */
    @GetMapping("/count/{status}")
    public Result<Integer> countByStatus(@PathVariable String status) {
        Integer count = jobAuditService.countByStatus(status);
        return Result.success(count);
    }
}