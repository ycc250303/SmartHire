package com.SmartHire.hrService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.enums.UserType;
import com.SmartHire.hrService.dto.JobInfoCreateDTO;
import com.SmartHire.hrService.dto.JobInfoListDTO;
import com.SmartHire.hrService.dto.JobInfoUpdateDTO;
import com.SmartHire.hrService.service.JobInfoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 岗位管理控制器 */
@RestController
@RequestMapping("/hr/job-position")
@Validated
@RequireUserType(UserType.HR) // 整个Controller要求HR身份
public class JobInfoController {

  @Autowired private JobInfoService jobInfoService;

  /**
   * 发布岗位
   *
   * @param createDTO 岗位创建信息
   * @return 岗位ID
   */
  @PostMapping
  @Operation(summary = "岗位发布", description = "发布新的招聘岗位")
  public Result<Long> createJobInfo(@Valid @RequestBody JobInfoCreateDTO createDTO) {
    Long jobId = jobInfoService.createJobInfo(createDTO);
    return Result.success("岗位发布成功", jobId);
  }

  /**
   * 更新岗位信息
   *
   * @param jobId 岗位ID
   * @param updateDTO 更新信息
   * @return 操作结果
   */
  @PatchMapping("/{jobId}")
  @Operation(summary = "岗位编辑", description = "编辑岗位信息")
  public Result<?> updateJobInfo(
      @PathVariable Long jobId, @Valid @RequestBody JobInfoUpdateDTO updateDTO) {
    jobInfoService.updateJobInfo(jobId, updateDTO);
    return Result.success("岗位更新成功");
  }

  /**
   * 岗位下线
   *
   * @param jobId 岗位ID
   * @return 操作结果
   */
  @PatchMapping("/{jobId}/offline")
  @Operation(summary = "岗位下线", description = "将岗位状态设置为已下线")
  public Result<?> offlineJobInfo(@PathVariable Long jobId) {
    jobInfoService.offlineJobInfo(jobId);
    return Result.success("岗位已下线");
  }

  /**
   * 岗位列表查询
   *
   * @param status 状态筛选（可选）：0-已下线 1-招聘中 2-已暂停
   * @return 岗位列表
   */
  @GetMapping
  @Operation(summary = "岗位列表查询", description = "查询当前HR的岗位列表，支持按状态筛选")
  public Result<List<JobInfoListDTO>> getJobInfoList(
      @RequestParam(required = false)
          @Min(value = 0, message = "状态值必须在0-2之间")
          @Max(value = 2, message = "状态值必须在0-2之间")
          Integer status) {
    List<JobInfoListDTO> list = jobInfoService.getJobInfoList(status);
    return Result.success("查询成功", list);
  }

  /**
   * 更新岗位状态
   *
   * @param jobId 岗位ID
   * @param status 状态：0-已下线 1-招聘中 2-已暂停
   * @return 操作结果
   */
  @PatchMapping("/{jobId}/status")
  @Operation(summary = "岗位状态管理", description = "更新岗位状态（招聘中、已结束等）")
  public Result<?> updateJobInfoStatus(
      @PathVariable Long jobId,
      @RequestParam
          @Min(value = 0, message = "状态值必须在0-2之间")
          @Max(value = 2, message = "状态值必须在0-2之间")
          Integer status) {
    jobInfoService.updateJobInfoStatus(jobId, status);
    return Result.success("状态更新成功");
  }

  /**
   * 获取岗位详情
   *
   * @param jobId 岗位ID
   * @return 岗位详情
   */
  @GetMapping("/{jobId}")
  @Operation(summary = "获取岗位详情", description = "根据ID获取岗位详细信息（包含技能要求）")
  public Result<JobInfoListDTO> getJobInfoById(@PathVariable Long jobId) {
    JobInfoListDTO jobInfo = jobInfoService.getJobInfoById(jobId);
    return Result.success("查询成功", jobInfo);
  }
}
