package com.SmartHire.hrService.controller;

import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.dto.HrInfoUpdateDTO;
import com.SmartHire.hrService.service.HrInfoService;
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
public class HrInfoController {

    @Autowired
    private HrInfoService hrInfoService;

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

