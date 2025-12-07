package com.SmartHire.seekerService.controller;

import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.enums.UserType;
import com.SmartHire.seekerService.dto.JobFavoriteDTO;
import com.SmartHire.seekerService.dto.SeekerDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.JobSeekerExpectationDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.ProjectExperienceDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.ResumeDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.SkillDTO;
import com.SmartHire.seekerService.dto.seekerTableDto.WorkExperienceDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.service.*;
import com.SmartHire.seekerService.service.seekerTableService.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 求职者服务控制器（对内） 仅限求职者本人使用，用于管理自己的个人信息、简历、技能等
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@RestController
@RequestMapping("/seeker")
@RequireUserType(UserType.SEEKER)
public class SeekerController {
  @Autowired private EducationExperienceService educationExperienceService;

  @Autowired private JobSeekerService jobSeekerService;

  @Autowired private JobSeekerExpectationService jobSeekerExpectationService;

  @Autowired private ProjectExperienceService projectExperienceService;

  @Autowired private ResumeService resumeService;

  @Autowired private WorkExperienceService workExperienceService;

  @Autowired private SkillService skillService;

  @Autowired private JobFavoriteService jobFavoriteService;

  /**
   * 注册求职者
   *
   * @param request
   * @return
   */
  @PostMapping("/register-seeker")
  @Operation(summary = "注册求职者", description = "填写求职者信息")
  public Result<?> registerSeeker(@Valid @RequestBody SeekerDTO request) {
    jobSeekerService.registerSeeker(request);
    return Result.success("求职者信息注册成功");
  }

  /**
   * 获取求职者信息
   *
   * @return
   */
  @GetMapping("/get-seeker-info")
  @Operation(summary = "获取求职者信息", description = "获取求职者信息")
  public Result<JobSeeker> getSeekerInfo() {
    JobSeeker jobSeeker = jobSeekerService.getSeekerInfo();
    return Result.success("获取求职者信息成功", jobSeeker);
  }

  /**
   * 更新求职者信息
   *
   * @param request 更新求职者信息
   * @return
   */
  @PatchMapping("/update-seeker-info")
  @Operation(summary = "更新求职者信息", description = "更新求职者信息，只需要DTO中的一项不为空即可")
  public Result<?> updateSeekerInfo(
      @Validated(SeekerDTO.Update.class) @RequestBody SeekerDTO request) {
    jobSeekerService.updateSeekerInfo(request);
    return Result.success("更新求职者信息成功");
  }

  /** 删除求职者信息 */
  @DeleteMapping("/delete-seeker")
  @Operation(summary = "删除求职者信息", description = "删除求职者信息及所有相关数据（级联删除）")
  public Result<?> deleteSeeker() {
    jobSeekerService.deleteJobSeeker();
    return Result.success("删除求职者信息成功");
  }

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

  /**
   * 修改教育经历
   *
   * @param id
   * @param request
   * @return
   */
  @PatchMapping("/update-education-experience/{id}")
  @Operation(summary = "修改教育经历", description = "修改教育经历，只需要DTO中的一项不为空即可")
  public Result<?> updateEducationExperience(
      @PathVariable Long id,
      @Validated(EducationExperienceDTO.Update.class) @RequestBody EducationExperienceDTO request) {
    educationExperienceService.updateEducationExperience(id, request);
    return Result.success("修改教育经历成功");
  }

  /**
   * 删除教育经历
   *
   * @param id
   * @return
   */
  @DeleteMapping("/delete-education-experience/{id}")
  @Operation(summary = "删除教育经历", description = "删除指定的教育经历")
  public Result<?> deleteEducationExperience(@PathVariable Long id) {
    educationExperienceService.deleteEducationExperience(id);
    return Result.success("删除教育经历成功");
  }

  /**
   * 添加项目经历
   *
   * @param request
   * @return
   */
  @PostMapping("/add-project-experience")
  @Operation(summary = "添加项目经历", description = "新增项目经历")
  public Result<?> addProjectExperience(
      @Validated(ProjectExperienceDTO.Create.class) @RequestBody ProjectExperienceDTO request) {
    projectExperienceService.addProjectExperience(request);
    return Result.success("添加项目经历成功");
  }

  /**
   * 获取项目经历列表
   *
   * @return
   */
  @GetMapping("/get-project-experiences")
  @Operation(summary = "获取项目经历列表", description = "获取当前用户的所有项目经历")
  public Result<?> getProjectExperiences() {
    return Result.success("获取项目经历成功", projectExperienceService.getProjectExperiences());
  }

  /**
   * 修改项目经历
   *
   * @param id
   * @param request
   * @return
   */
  @PatchMapping("/update-project-experience/{id}")
  @Operation(summary = "修改项目经历", description = "修改项目经历，只需要DTO中的一项不为空即可")
  public Result<?> updateProjectExperience(
      @PathVariable Long id,
      @Validated(ProjectExperienceDTO.Update.class) @RequestBody ProjectExperienceDTO request) {
    projectExperienceService.updateProjectExperience(id, request);
    return Result.success("修改项目经历成功");
  }

  /**
   * 删除项目经历
   *
   * @param id
   * @return
   */
  @DeleteMapping("/delete-project-experience/{id}")
  @Operation(summary = "删除项目经历", description = "删除指定的项目经历")
  public Result<?> deleteProjectExperience(@PathVariable Long id) {
    projectExperienceService.deleteProjectExperience(id);
    return Result.success("删除项目经历成功");
  }

  /**
   * 添加工作/实习经历
   *
   * @param request
   * @return
   */
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

  /**
   * 修改工作/实习经历
   *
   * @param
   */
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

  /** 添加技能 */
  @PostMapping("/add-skill")
  @Operation(summary = "添加技能", description = "新增技能信息")
  public Result<?> addSkill(@Validated(SkillDTO.Create.class) @RequestBody SkillDTO request) {
    skillService.addSkill(request);
    return Result.success("添加技能成功");
  }

  /** 获取技能列表 */
  @GetMapping("/get-skills")
  @Operation(summary = "获取技能列表", description = "获取当前用户的所有技能")
  public Result<?> getSkills() {
    return Result.success("获取技能成功", skillService.getSkills());
  }

  /** 更新技能 */
  @PatchMapping("/update-skill/{id}")
  @Operation(summary = "更新技能", description = "部分更新技能信息")
  public Result<?> updateSkill(
      @PathVariable Long id, @Validated(SkillDTO.Update.class) @RequestBody SkillDTO request) {
    skillService.updateSkill(id, request);
    return Result.success("修改技能成功");
  }

  /** 删除技能 */
  @DeleteMapping("/delete-skill/{id}")
  @Operation(summary = "删除技能", description = "删除指定技能")
  public Result<?> deleteSkill(@PathVariable Long id) {
    skillService.deleteSkill(id);
    return Result.success("删除技能成功");
  }

  /** 上传附件简历 */
  @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "上传附件简历", description = "上传附件简历，名称默认取原文件名，最多5个")
  public Result<?> uploadResume(@RequestBody MultipartFile resumeFile) {
    resumeService.uploadResume(resumeFile);
    return Result.success("上传简历成功");
  }

  /** 获取附件简历列表 */
  @GetMapping("/get-resumes")
  @Operation(summary = "获取附件简历列表", description = "一次返回当前求职者的所有附件简历")
  public Result<?> getResumes() {
    return Result.success("获取简历成功", resumeService.getResumes());
  }

  /** 更新附件简历 */
  @PatchMapping("/update-resume/{id}")
  @Operation(summary = "更新附件简历", description = "仅支持修改名称和隐私级别，至少一项有变动")
  public Result<?> updateResume(
      @PathVariable Long id, @Validated(ResumeDTO.Update.class) @RequestBody ResumeDTO request) {
    resumeService.updateResume(id, request);
    return Result.success("更新简历成功");
  }

  /** 删除附件简历 */
  @DeleteMapping("/delete-resume/{id}")
  @Operation(summary = "删除附件简历", description = "删除指定的附件简历")
  public Result<?> deleteResume(@PathVariable Long id) {
    resumeService.deleteResume(id);
    return Result.success("删除简历成功");
  }

  /**
   * 添加求职期望
   *
   * @param request
   * @return
   */
  @PostMapping("/add-job-seeker-expectation")
  @Operation(summary = "添加求职期望", description = "添加求职期望，最多5个")
  public Result<?> addJobSeekerExpectation(
      @Validated(JobSeekerExpectationDTO.Create.class) @RequestBody
          JobSeekerExpectationDTO request) {
    jobSeekerExpectationService.addJobSeekerExpectation(request);
    return Result.success("添加求职期望成功");
  }

  /**
   * 获取求职期望列表
   *
   * @return
   */
  @GetMapping("/get-job-seeker-expectations")
  @Operation(summary = "获取求职期望列表", description = "获取当前用户的所有求职期望")
  public Result<?> getJobSeekerExpectations() {
    return Result.success("获取求职期望成功", jobSeekerExpectationService.getJobSeekerExpectations());
  }

  /**
   * 修改求职期望
   *
   * @param id
   * @param request
   * @return
   */
  @PatchMapping("/update-job-seeker-expectation/{id}")
  @Operation(summary = "修改求职期望", description = "修改求职期望，只需要DTO中的一项不为空即可")
  public Result<?> updateJobSeekerExpectation(
      @PathVariable Long id,
      @Validated(JobSeekerExpectationDTO.Update.class) @RequestBody
          JobSeekerExpectationDTO request) {
    jobSeekerExpectationService.updateJobSeekerExpectation(id, request);
    return Result.success("修改求职期望成功");
  }

  /**
   * 删除求职期望
   *
   * @param id
   * @return
   */
  @DeleteMapping("/delete-job-seeker-expectation/{id}")
  @Operation(summary = "删除求职期望", description = "删除指定的求职期望")
  public Result<?> deleteJobSeekerExpectation(@PathVariable Long id) {
    jobSeekerExpectationService.deleteJobSeekerExpectation(id);
    return Result.success("删除求职期望成功");
  }

  /**
   * 收藏岗位
   *
   * @param jobId 岗位ID
   * @return 操作结果
   */
  @PostMapping("/favorite-job/{jobId}")
  @Operation(summary = "收藏岗位", description = "求职者收藏指定岗位")
  public Result<?> addJobFavorite(
      @PathVariable @NotNull(message = "岗位ID不能为空") @Positive(message = "岗位ID必须为正整数") Long jobId) {
    jobFavoriteService.addJobFavorite(jobId);
    return Result.success("收藏岗位成功");
  }

  /**
   * 取消收藏岗位
   *
   * @param jobId 岗位ID
   * @return 操作结果
   */
  @DeleteMapping("/favorite-job/{jobId}")
  @Operation(summary = "取消收藏岗位", description = "求职者取消收藏指定岗位")
  public Result<?> removeJobFavorite(
      @PathVariable @NotNull(message = "岗位ID不能为空") @Positive(message = "岗位ID必须为正整数") Long jobId) {
    jobFavoriteService.removeJobFavorite(jobId);
    return Result.success("取消收藏成功");
  }

  /**
   * 获取收藏岗位列表
   *
   * @return 收藏岗位列表
   */
  @GetMapping("/favorite-jobs")
  @Operation(summary = "获取收藏岗位列表", description = "获取当前求职者的所有收藏岗位")
  public Result<List<JobFavoriteDTO>> getJobFavoriteList() {
    List<JobFavoriteDTO> favorites = jobFavoriteService.getJobFavoriteList();
    return Result.success("获取收藏岗位列表成功", favorites);
  }
}
