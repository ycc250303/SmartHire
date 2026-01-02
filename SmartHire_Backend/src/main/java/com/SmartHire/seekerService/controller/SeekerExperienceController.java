package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.ProjectExperienceDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.WorkExperienceDTO;
import com.SmartHire.seekerService.service.seekerTableService.EducationExperienceService;
import com.SmartHire.seekerService.service.seekerTableService.ProjectExperienceService;
import com.SmartHire.seekerService.service.seekerTableService.WorkExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 求职者服务经历管理控制器
 *
 * @author SmartHire Team
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
@RequireUserType({UserType.SEEKER})
public class SeekerExperienceController {

    @Autowired
    private EducationExperienceService educationExperienceService;

    @Autowired
    private ProjectExperienceService projectExperienceService;

    @Autowired
    private WorkExperienceService workExperienceService;

    /** 添加教育经历 */
    @PostMapping("/add-education-experience")
    @Operation(summary = "添加教育经历", description = "添加教育经历")
    public Result<?> addEducationExperience(
            @Validated(EducationExperienceDTO.Create.class) @RequestBody EducationExperienceDTO request) {
        educationExperienceService.addEducationExperience(request);
        return Result.success("添加教育经历成功");
    }

    /** 获取教育经历列表 */
    @GetMapping("/get-education-experiences")
    @Operation(summary = "获取教育经历列表", description = "获取当前用户的所有教育经历")
    public Result<?> getEducationExperiences() {
        return Result.success("获取教育经历成功", educationExperienceService.getEducationExperiences());
    }

    /** 修改教育经历 */
    @PatchMapping("/update-education-experience/{id}")
    @Operation(summary = "修改教育经历", description = "修改教育经历，只需要DTO中的一项不为空即可")
    public Result<?> updateEducationExperience(
            @PathVariable Long id,
            @Validated(EducationExperienceDTO.Update.class) @RequestBody EducationExperienceDTO request) {
        educationExperienceService.updateEducationExperience(id, request);
        return Result.success("修改教育经历成功");
    }

    /** 删除教育经历 */
    @DeleteMapping("/delete-education-experience/{id}")
    @Operation(summary = "删除教育经历", description = "删除指定的教育经历")
    public Result<?> deleteEducationExperience(@PathVariable Long id) {
        educationExperienceService.deleteEducationExperience(id);
        return Result.success("删除教育经历成功");
    }

    /** 添加项目经历 */
    @PostMapping("/add-project-experience")
    @Operation(summary = "添加项目经历", description = "新增项目经历")
    public Result<?> addProjectExperience(
            @Validated(ProjectExperienceDTO.Create.class) @RequestBody ProjectExperienceDTO request) {
        projectExperienceService.addProjectExperience(request);
        return Result.success("添加项目经历成功");
    }

    /** 获取项目经历列表 */
    @GetMapping("/get-project-experiences")
    @Operation(summary = "获取项目经历列表", description = "获取当前用户的所有项目经历")
    public Result<?> getProjectExperiences() {
        return Result.success("获取项目经历成功", projectExperienceService.getProjectExperiences());
    }

    /** 修改项目经历 */
    @PatchMapping("/update-project-experience/{id}")
    @Operation(summary = "修改项目经历", description = "修改项目经历，只需要DTO中的一项不为空即可")
    public Result<?> updateProjectExperience(
            @PathVariable Long id,
            @Validated(ProjectExperienceDTO.Update.class) @RequestBody ProjectExperienceDTO request) {
        projectExperienceService.updateProjectExperience(id, request);
        return Result.success("修改项目经历成功");
    }

    /** 删除项目经历 */
    @DeleteMapping("/delete-project-experience/{id}")
    @Operation(summary = "删除项目经历", description = "删除指定的项目经历")
    public Result<?> deleteProjectExperience(@PathVariable Long id) {
        projectExperienceService.deleteProjectExperience(id);
        return Result.success("删除项目经历成功");
    }

    /** 添加工作/实习经历 */
    @PostMapping("/add-work-experience")
    @Operation(summary = "添加工作/实习经历", description = "新增工作或实习经历")
    public Result<?> addWorkExperience(
            @Validated(WorkExperienceDTO.Create.class) @RequestBody WorkExperienceDTO request) {
        workExperienceService.addWorkExperience(request);
        return Result.success("添加工作/实习经历成功");
    }

    /** 获取工作/实习经历列表 */
    @GetMapping("/get-work-experiences")
    @Operation(summary = "获取工作/实习经历列表", description = "获取当前用户的所有工作/实习经历")
    public Result<?> getWorkExperiences() {
        return Result.success("获取工作/实习经历成功", workExperienceService.getWorkExperiences());
    }

    /** 修改工作/实习经历 */
    @PatchMapping("/update-work-experience/{id}")
    @Operation(summary = "修改工作/实习经历", description = "部分更新工作/实习经历")
    public Result<?> updateWorkExperience(
            @PathVariable Long id,
            @Validated(WorkExperienceDTO.Update.class) @RequestBody WorkExperienceDTO request) {
        workExperienceService.updateWorkExperience(id, request);
        return Result.success("修改工作/实习经历成功");
    }

    /** 删除工作/实习经历 */
    @DeleteMapping("/delete-work-experience/{id}")
    @Operation(summary = "删除工作/实习经历", description = "删除指定的工作/实习经历")
    public Result<?> deleteWorkExperience(@PathVariable Long id) {
        workExperienceService.deleteWorkExperience(id);
        return Result.success("删除工作/实习经历成功");
    }
}

