package com.SmartHire.hrService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.service.CompanyAdminService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * HR服务公司管理控制器
 *
 * @author SmartHire Team
 * @since 2025-12-13
 */
@RestController
@RequestMapping("/hr/company")
@Validated
@RequireUserType(UserType.HR) // 要求HR身份，Service层会验证是否为公司管理员
public class CompanyAdminController {

  @Autowired private CompanyAdminService companyAdminService;

  /**
   * 修改公司信息
   *
   * @param companyId 公司ID
   * @param company 公司信息
   * @return 操作结果
   */
  @PutMapping("/{companyId}/info")
  @Operation(summary = "修改公司信息", description = "公司管理员修改本公司信息")
  public Result<?> updateCompanyInfo(
      @PathVariable Long companyId, @Valid @RequestBody Company company) {
    companyAdminService.updateCompanyInfo(companyId, company);
    return Result.success("公司信息更新成功");
  }

  /**
   * 获取本公司HR列表
   *
   * @param current 当前页
   * @param size 每页大小
   * @param keyword 搜索关键词（可选）
   * @return HR列表
   */
  @GetMapping("/hr-list")
  @Operation(summary = "获取本公司HR列表", description = "公司管理员查看本公司所有HR")
  public Result<Page<HrInfoDTO>> getCompanyHrList(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String keyword) {
    Page<HrInfoDTO> page = new Page<>(current, size);
    Page<HrInfoDTO> result = companyAdminService.getCompanyHrList(page, keyword);
    return Result.success("查询成功", result);
  }

  /**
   * 审核通过HR
   *
   * @param hrId HR ID
   * @return 操作结果
   */
  @PostMapping("/hr/{hrId}/approve")
  @Operation(summary = "审核通过HR", description = "公司管理员审核通过HR")
  public Result<?> approveHr(@PathVariable Long hrId) {
    companyAdminService.approveHr(hrId);
    return Result.success("HR审核通过");
  }

  /**
   * 审核拒绝HR
   *
   * @param hrId HR ID
   * @param rejectReason 拒绝原因
   * @return 操作结果
   */
  @PostMapping("/hr/{hrId}/reject")
  @Operation(summary = "审核拒绝HR", description = "公司管理员审核拒绝HR")
  public Result<?> rejectHr(
      @PathVariable Long hrId, @RequestParam @NotBlank(message = "拒绝原因不能为空") String rejectReason) {
    companyAdminService.rejectHr(hrId, rejectReason);
    return Result.success("HR审核已拒绝");
  }

  /**
   * 获取本公司待审核岗位列表
   *
   * @param current 当前页
   * @param size 每页大小
   * @param status 审核状态（可选）
   * @param keyword 搜索关键词（可选）
   * @return 岗位审核列表
   */
  @GetMapping("/job-audit")
  @Operation(summary = "获取本公司待审核岗位列表", description = "公司管理员查看本公司待审核的岗位")
  public Result<Page<com.SmartHire.adminService.dto.JobAuditListDTO>> getCompanyJobAuditList(
      @RequestParam(defaultValue = "1") Long current,
      @RequestParam(defaultValue = "10") Long size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Page<com.SmartHire.adminService.dto.JobAuditListDTO> page = new Page<>(current, size);
    Page<com.SmartHire.adminService.dto.JobAuditListDTO> result =
        companyAdminService.getCompanyJobAuditList(page, status, keyword);
    return Result.success("查询成功", result);
  }

  /**
   * 审核通过岗位（公司审核）
   *
   * @param jobId 岗位ID
   * @return 操作结果
   */
  @PostMapping("/job-audit/{jobId}/approve")
  @Operation(summary = "审核通过岗位", description = "公司管理员审核通过岗位，通过后进入系统审核")
  public Result<?> approveCompanyJob(@PathVariable Long jobId) {
    companyAdminService.approveCompanyJob(jobId);
    return Result.success("岗位审核通过，已提交系统审核");
  }

  /**
   * 审核拒绝岗位（公司审核）
   *
   * @param jobId 岗位ID
   * @param rejectReason 拒绝原因
   * @return 操作结果
   */
  @PostMapping("/job-audit/{jobId}/reject")
  @Operation(summary = "审核拒绝岗位", description = "公司管理员审核拒绝岗位")
  public Result<?> rejectCompanyJob(
      @PathVariable Long jobId, @RequestParam @NotBlank(message = "拒绝原因不能为空") String rejectReason) {
    companyAdminService.rejectCompanyJob(jobId, rejectReason);
    return Result.success("岗位审核已拒绝");
  }

  /**
   * 要求修改岗位（公司审核）
   *
   * @param jobId 岗位ID
   * @param modifyReason 修改意见
   * @return 操作结果
   */
  @PostMapping("/job-audit/{jobId}/modify")
  @Operation(summary = "要求修改岗位", description = "公司管理员要求修改岗位")
  public Result<?> modifyCompanyJob(
      @PathVariable Long jobId, @RequestParam @NotBlank(message = "修改意见不能为空") String modifyReason) {
    companyAdminService.modifyCompanyJob(jobId, modifyReason);
    return Result.success("已要求修改岗位");
  }
}

