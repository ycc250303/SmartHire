package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.seekerService.dto.JobFavoriteDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.JobSeekerExpectationDTO;
import com.SmartHire.seekerService.service.JobFavoriteService;
import com.SmartHire.seekerService.service.seekerTableService.JobSeekerExpectationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 求职者服务意向与收藏控制器
 *
 * @author SmartHire Team
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
@RequireUserType({UserType.SEEKER})
public class SeekerJobController {

    @Autowired
    private JobSeekerExpectationService jobSeekerExpectationService;

    @Autowired
    private JobFavoriteService jobFavoriteService;

    /** 添加求职期望 */
    @PostMapping("/add-job-seeker-expectation")
    @Operation(summary = "添加求职期望", description = "添加求职期望，最多5个")
    public Result<?> addJobSeekerExpectation(
            @Validated(JobSeekerExpectationDTO.Create.class) @RequestBody JobSeekerExpectationDTO request) {
        jobSeekerExpectationService.addJobSeekerExpectation(request);
        return Result.success("添加求职期望成功");
    }

    /** 获取求职期望列表 */
    @GetMapping("/get-job-seeker-expectations")
    @Operation(summary = "获取求职期望列表", description = "获取当前用户的所有求职期望")
    public Result<?> getJobSeekerExpectations() {
        return Result.success("获取求职期望成功", jobSeekerExpectationService.getJobSeekerExpectations());
    }

    /** 修改求职期望 */
    @PatchMapping("/update-job-seeker-expectation/{id}")
    @Operation(summary = "修改求职期望", description = "修改求职期望，只需要DTO中的一项不为空即可")
    public Result<?> updateJobSeekerExpectation(
            @PathVariable Long id,
            @Validated(JobSeekerExpectationDTO.Update.class) @RequestBody JobSeekerExpectationDTO request) {
        jobSeekerExpectationService.updateJobSeekerExpectation(id, request);
        return Result.success("修改求职期望成功");
    }

    /** 删除求职期望 */
    @DeleteMapping("/delete-job-seeker-expectation/{id}")
    @Operation(summary = "删除求职期望", description = "删除指定的求职期望")
    public Result<?> deleteJobSeekerExpectation(@PathVariable Long id) {
        jobSeekerExpectationService.deleteJobSeekerExpectation(id);
        return Result.success("删除求职期望成功");
    }

    /** 收藏岗位 */
    @PostMapping("/favorite-job/{jobId}")
    @Operation(summary = "收藏岗位", description = "求职者收藏指定岗位")
    public Result<?> addJobFavorite(
            @PathVariable @NotNull(message = "岗位ID不能为空") @Positive(message = "岗位ID必须为正整数") Long jobId) {
        jobFavoriteService.addJobFavorite(jobId);
        return Result.success("收藏岗位成功");
    }

    /** 取消收藏岗位 */
    @DeleteMapping("/favorite-job/{jobId}")
    @Operation(summary = "取消收藏岗位", description = "求职者取消收藏指定岗位")
    public Result<?> removeJobFavorite(
            @PathVariable @NotNull(message = "岗位ID不能为空") @Positive(message = "岗位ID必须为正整数") Long jobId) {
        jobFavoriteService.removeJobFavorite(jobId);
        return Result.success("取消收藏成功");
    }

    /** 获取收藏岗位列表 */
    @GetMapping("/favorite-jobs")
    @Operation(summary = "获取收藏岗位列表", description = "获取当前求职者的所有收藏岗位")
    public Result<List<JobFavoriteDTO>> getJobFavoriteList() {
        List<JobFavoriteDTO> favorites = jobFavoriteService.getJobFavoriteList();
        return Result.success("获取收藏岗位列表成功", favorites);
    }
}

