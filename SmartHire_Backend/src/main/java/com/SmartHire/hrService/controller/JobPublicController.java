package com.SmartHire.hrService.controller;

import com.SmartHire.common.entity.Result;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.common.dto.hrDto.JobSearchDTO;
import com.SmartHire.hrService.service.JobInfoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HR服务岗位公共展示控制器
 */
@RestController
@RequestMapping("/public/job-position")
@Validated
public class JobPublicController {

  @Autowired private JobInfoService jobInfoService;

  @PostMapping("/search")
  @Operation(summary = "岗位筛选（求职者端）", description = "按条件筛选招聘中的岗位列表")
  public Result<List<JobCardDTO>> searchPublicJobs(@Valid @RequestBody JobSearchDTO searchDTO) {
    List<JobCardDTO> list = jobInfoService.searchPublicJobs(searchDTO);
    return Result.success("查询成功", list);
  }
}

