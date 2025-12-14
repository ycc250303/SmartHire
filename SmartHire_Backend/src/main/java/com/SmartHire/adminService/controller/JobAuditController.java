package com.SmartHire.adminService.controller;

import com.SmartHire.adminService.dto.JobAuditDTO;
import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.SmartHire.adminService.dto.JobAuditQueryDTO;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.SmartHire.adminService.service.JobAuditService;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.enums.UserType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 职位审核控制器 参考用户封禁功能的实现方式
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/review")
@RequireUserType(UserType.ADMIN) // 只有管理员可以访问职位审核功能
@Validated
public class JobAuditController {

  @Autowired private JobAuditService jobAuditService;

  @Autowired private UserContext userContext;

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
    Long auditorId = userContext.getCurrentUserId();
    String auditorName = userContext.getCurrentUsername();

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
  public Result<?> rejectJob(@PathVariable Long jobId, @Valid @RequestBody JobAuditDTO auditDTO) {
    // 从SecurityContext获取当前管理员信息
    Long auditorId = userContext.getCurrentUserId();
    String auditorName = userContext.getCurrentUsername();

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
  public Result<?> modifyJob(@PathVariable Long jobId, @Valid @RequestBody JobAuditDTO auditDTO) {
    // 从SecurityContext获取当前管理员信息
    Long auditorId = userContext.getCurrentUserId();
    String auditorName = userContext.getCurrentUsername();

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
     * 强制下线职位
     *
     * @param jobId 职位ID
     * @param auditDTO 审核DTO（包含下线原因）
     * @return 操作结果
     */
    @PostMapping("/jobs/{jobId}/offline")
    public Result<?> forceOfflineJob(@PathVariable Long jobId,
                                   @Valid @RequestBody JobAuditDTO auditDTO) {
        // 从SecurityContext获取当前管理员信息
        Long auditorId = userContext.getCurrentUserId();
        String auditorName = userContext.getCurrentUsername();

        jobAuditService.forceOfflineJob(jobId, auditDTO.getReason(), auditorId, auditorName);
        return Result.success("职位已强制下线");
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
