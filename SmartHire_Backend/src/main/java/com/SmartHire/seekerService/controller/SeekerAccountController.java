package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.seekerService.dto.SeekerDTO;
import com.SmartHire.seekerService.dto.SeekerInfoDTO;
import com.SmartHire.seekerService.service.JobSeekerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 求职者服务账户与基本信息控制器
 *
 * @author SmartHire Team
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
@RequireUserType({UserType.SEEKER})
public class SeekerAccountController {

    @Autowired
    private JobSeekerService jobSeekerService;

    /**
     * 注册求职者
     */
    @PostMapping("/register-seeker")
    @Operation(summary = "注册求职者", description = "填写求职者信息")
    public Result<?> registerSeeker(@Valid @RequestBody SeekerDTO request) {
        jobSeekerService.registerSeeker(request);
        return Result.success("求职者信息注册成功");
    }

    /**
     * 获取求职者信息
     */
    @GetMapping("/get-seeker-info")
    @Operation(summary = "获取求职者信息", description = "获取求职者信息")
    public Result<SeekerInfoDTO> getSeekerInfo() {
        SeekerInfoDTO seekerInfo = jobSeekerService.getSeekerInfo();
        return Result.success("获取求职者信息成功", seekerInfo);
    }

    /**
     * 更新求职者信息
     */
    @PatchMapping("/update-seeker-info")
    @Operation(summary = "更新求职者信息", description = "更新求职者信息，只需要DTO中的一项不为空即可")
    public Result<?> updateSeekerInfo(
            @Validated(SeekerDTO.Update.class) @RequestBody SeekerDTO request) {
        jobSeekerService.updateSeekerInfo(request);
        return Result.success("更新求职者信息成功");
    }

    /**
     * 删除求职者信息
     */
    @DeleteMapping("/delete-seeker")
    @Operation(summary = "删除求职者信息", description = "删除求职者信息及所有相关数据（级联删除）")
    public Result<?> deleteSeeker() {
        jobSeekerService.deleteJobSeeker();
        return Result.success("删除求职者信息成功");
    }
}

