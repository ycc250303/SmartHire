package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.seekerService.dto.seekerTableDto.ResumeDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.SkillDTO;
import com.SmartHire.seekerService.service.seekerTableService.SkillService;
import com.SmartHire.seekerService.service.seekerTableService.ResumeService;
import com.SmartHire.seekerService.service.OnlineResumeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 求职者服务简历与技能控制器
 *
 * @author SmartHire Team
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
public class SeekerResumeController {

    @Autowired
    private SkillService skillService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private OnlineResumeService onlineResumeService;

    /** 添加技能 */
    @PostMapping("/add-skill")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "添加技能", description = "新增技能信息")
    public Result<?> addSkill(@Validated(SkillDTO.Create.class) @RequestBody SkillDTO request) {
        skillService.addSkill(request);
        return Result.success("添加技能成功");
    }

    /** 获取技能列表 */
    @GetMapping("/get-skills")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "获取技能列表", description = "获取当前用户的所有技能")
    public Result<?> getSkills() {
        return Result.success("获取技能成功", skillService.getSkills());
    }

    /** 更新技能 */
    @PatchMapping("/update-skill/{id}")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "更新技能", description = "部分更新技能信息")
    public Result<?> updateSkill(
            @PathVariable Long id, @Validated(SkillDTO.Update.class) @RequestBody SkillDTO request) {
        skillService.updateSkill(id, request);
        return Result.success("修改技能成功");
    }

    /** 删除技能 */
    @DeleteMapping("/delete-skill/{id}")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "删除技能", description = "删除指定技能")
    public Result<?> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return Result.success("删除技能成功");
    }

    /** 上传附件简历 */
    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "上传附件简历", description = "上传附件简历，名称默认取原文件名，最多5个")
    public Result<?> uploadResume(@RequestBody MultipartFile resumeFile) {
        resumeService.uploadResume(resumeFile);
        return Result.success("上传简历成功");
    }

    /** 获取附件简历列表 */
    @GetMapping("/get-resumes")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "获取附件简历列表", description = "一次返回当前求职者的所有附件简历")
    public Result<?> getResumes() {
        return Result.success("获取简历成功", resumeService.getResumes());
    }

    /** 更新附件简历 */
    @PatchMapping("/update-resume/{id}")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "更新附件简历", description = "仅支持修改名称和隐私级别，至少一项有变动")
    public Result<?> updateResume(
            @PathVariable Long id, @Validated(ResumeDTO.Update.class) @RequestBody ResumeDTO request) {
        resumeService.updateResume(id, request);
        return Result.success("更新简历成功");
    }

    /** 删除附件简历 */
    @DeleteMapping("/delete-resume/{id}")
    @RequireUserType({UserType.SEEKER})
    @Operation(summary = "删除附件简历", description = "删除指定的附件简历")
    public Result<?> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return Result.success("删除简历成功");
    }

    /**
     * 获取指定用户的在线简历
     *
     * @param userId 用户ID
     * @return 在线简历聚合数据
     */
    @GetMapping("/online-resume")
    @RequireUserType({UserType.HR, UserType.SEEKER})
    @Operation(summary = "获取在线简历", description = "HR查看指定用户的在线简历")
    public Result<?> getOnlineResume(
            @RequestParam("userId") @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正整数") Long userId) {
        return Result.success("获取在线简历成功", onlineResumeService.getOnlineResumeByUserId(userId));
    }
}

