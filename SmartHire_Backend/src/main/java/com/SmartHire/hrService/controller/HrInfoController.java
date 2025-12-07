package com.SmartHire.hrService.controller;

import com.SmartHire.hrService.dto.HrInfoCreateDTO;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.service.HrInfoService;
import com.SmartHire.shared.annotation.RequireHr;
import com.SmartHire.shared.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * HR信息管理控制器
 */
@RestController
@RequestMapping("/hr/info")
@Validated
@RequireHr
public class HrInfoController {

    @Autowired
    private HrInfoService hrInfoService;

    /**
     * 注册HR信息
     *
     * @param createDTO 创建信息
     * @return HR信息ID
     */
    @PostMapping
    @Operation(summary = "注册HR信息", description = "为当前登录用户注册HR信息")
    public Result<Long> createHrInfo(@Valid @RequestBody HrInfoCreateDTO createDTO) {
        Long hrId = hrInfoService.createHrInfo(createDTO);
        return Result.success("HR信息注册成功", hrId);
    }

    /**
     * 获取当前登录HR的信息
     *
     * @return HR信息
     */
    @GetMapping
    @Operation(summary = "获取HR信息", description = "获取当前登录HR的完整信息（包含公司信息）")
    public Result<HrInfoDTO> getHrInfo() {
        HrInfoDTO hrInfo = hrInfoService.getHrInfo();
        return Result.success("获取HR信息成功", hrInfo);
    }

    /**
     * 更新当前登录HR的信息
     *
     * @param updateDTO 更新信息
     * @return 操作结果
     */
    @PatchMapping
    @Operation(summary = "更新HR信息", description = "更新当前登录HR的信息（真实姓名、职位、工作电话）")
    public Result<?> updateHrInfo(@Valid @RequestBody HrInfoUpdateDTO updateDTO) {
        hrInfoService.updateHrInfo(updateDTO);
        return Result.success("更新HR信息成功");
    }
}

