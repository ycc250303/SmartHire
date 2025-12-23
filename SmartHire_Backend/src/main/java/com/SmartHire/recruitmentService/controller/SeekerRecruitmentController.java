package com.SmartHire.recruitmentService.controller;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.auth.RequireUserType;
import com.SmartHire.common.auth.UserType;
import com.SmartHire.common.entity.Result;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.dto.JobSearchDTO;
import com.SmartHire.hrService.service.JobInfoService;
import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.seekerService.dto.SeekerCardDTO;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.common.auth.UserContext;
import com.SmartHire.recruitmentService.dto.InternJobItemDTO;
import com.SmartHire.recruitmentService.dto.InternJobRecommendationsDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.SmartHire.recruitmentService.dto.SeekerJobPositionDTO;
import com.SmartHire.recruitmentService.mapper.ApplicationMapper;
import com.SmartHire.recruitmentService.model.Application;
import com.SmartHire.hrService.dto.JobInfoListDTO;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.userAuthService.model.User;
import com.SmartHire.messageService.mapper.ChatMessageMapper;
import com.SmartHire.messageService.model.ChatMessage;
import com.SmartHire.recruitmentService.dto.SubmitResumeDTO;
import com.SmartHire.recruitmentService.service.ApplicationService;
import com.SmartHire.common.api.UserAuthApi;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投递岗位控制器
 *
 * @author SmartHire Team
 * @since 2025-11-30
 */
@RestController
@RequestMapping({"/recruitment/seeker", "/seeker"})
@RequireUserType(UserType.SEEKER)
@Validated
public class SeekerRecruitmentController {
  @Autowired
  @Qualifier("applicationServiceImpl")
  private ApplicationService applicationService;

  @Autowired private HrApi hrApi;
  @Autowired private SeekerApi seekerApi;
  @Autowired private JobInfoService jobInfoService;
  @Autowired private UserContext userContext;
  @Autowired private ApplicationMapper applicationMapper;
  @Autowired private ChatMessageMapper chatMessageMapper;
  @Autowired private UserAuthApi userAuthApi;

  @PostMapping("/submit-resume")
  @Operation(
      summary = "求职者投递简历",
      description = "求职者投递简历到指定职位。如果提供resumeId则投递附件简历，如果不提供resumeId则投递在线简历")
  public Result<?> submitResume(@Valid @RequestBody SubmitResumeDTO request) {
    Long applicationId = applicationService.submitResume(request);
    return Result.success("投递简历成功",applicationId);
  }

  @GetMapping("/job-card/{jobId}")
  @Operation(summary = "获取岗位卡片", description = "根据岗位ID获取岗位卡片信息（包含岗位、公司、HR信息）")
  public Result<JobCardDTO> getJobCard(
      @PathVariable @NotNull(message = "岗位ID不能为空") @Min(value = 1, message = "岗位ID必须为正整数")
          Long jobId) {
    JobCardDTO jobCard = hrApi.getJobCardByJobId(jobId);
    if (jobCard == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }
    return Result.success("获取岗位卡片成功", jobCard);
  }

  @GetMapping("/job-list")
  @Operation(summary = "获取投递过的列表", description = "获取投递过的职位列表")
  public Result<List<Long>> getJobIdListBySeekerId(){
      return Result.success("获取投递过的列表成功", applicationService.getJobIdListBySeekerId());
  }

  @GetMapping("/job-recommendations/intern")
  @Operation(summary = "获取实习岗位推荐", description = "获取用户首页推荐的实习岗位列表，优先使用简历/求职者信息进行关键词匹配计算得分（向量搜索未就绪）")
  public Result<InternJobRecommendationsDTO> getInternJobRecommendations(Long userId) {
    // Determine target user id: prefer query param, fallback to current user from context
    Long targetUserId = userId;
    if (targetUserId == null) {
      targetUserId = userContext.getCurrentUserId();
    }

    Long jobSeekerId = seekerApi.getJobSeekerIdByUserId(targetUserId);
    if (jobSeekerId == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }

    // Fetch seeker basic info to build keywords
    JobSeeker seeker = seekerApi.getJobSeekerById(jobSeekerId);
    SeekerCardDTO seekerCard = seekerApi.getSeekerCard(targetUserId);

    JobSearchDTO searchDTO = new JobSearchDTO();
    searchDTO.setJobType(1); // 1 == internship
    if (seeker != null && seeker.getCurrentCity() != null) {
      searchDTO.setCity(seeker.getCurrentCity());
    } else if (seekerCard != null && seekerCard.getCity() != null) {
      searchDTO.setCity(seekerCard.getCity());
    }
    if (seeker != null && seeker.getEducation() != null) {
      searchDTO.setEducationRequired(seeker.getEducation());
    }
    // derive a simple keyword from major/university/education
    StringBuilder kw = new StringBuilder();
    if (seekerCard != null) {
      if (seekerCard.getMajor() != null) {
        kw.append(seekerCard.getMajor()).append(" ");
      }
      if (seekerCard.getHighestEducation() != null) {
        kw.append(seekerCard.getHighestEducation()).append(" ");
      }
      if (seekerCard.getUniversity() != null) {
        kw.append(seekerCard.getUniversity()).append(" ");
      }
    }
    String keyword = kw.toString().trim();
    if (!keyword.isEmpty()) {
      searchDTO.setKeyword(keyword);
    }

    // fetch a reasonable page of internship jobs (<=12 as API docs)
    searchDTO.setPage(1);
    searchDTO.setSize(12);

    List<JobCardDTO> jobCards = jobInfoService.searchPublicJobs(searchDTO);

    List<InternJobItemDTO> items = new ArrayList<>();
    for (JobCardDTO jc : jobCards) {
      InternJobItemDTO item = new InternJobItemDTO();
      item.setJobId(jc.getJobId());
      item.setTitle(jc.getJobTitle());
      item.setCompanyName(jc.getCompanyName());
      item.setCity(jc.getCity());
      item.setAddress(jc.getAddress());
      item.setSalary_min(jc.getSalaryMin() == null ? 0 : jc.getSalaryMin().intValue());
      item.setSalary_max(jc.getSalaryMax() == null ? 0 : jc.getSalaryMax().intValue());

      // simple keyword-based matching score (since vector DB not ready)
      int score = 50;
      String lowerKeyword = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
      String combined = (Objects.toString(jc.getJobTitle(), "") + " " + Objects.toString(jc.getCompanyName(), "") + " " + Objects.toString(jc.getCity(), "")).toLowerCase(Locale.ROOT);
      if (!lowerKeyword.isEmpty()) {
        if (combined.contains(lowerKeyword)) {
          score += 30;
        } else {
          // partial token match
          String[] toks = lowerKeyword.split("\\s+");
          for (String t : toks) {
            if (!t.isEmpty() && combined.contains(t)) {
              score += 10;
            }
          }
        }
      }
      // education / city bonus
      if (seeker != null && seeker.getEducation() != null && jc.getEducationRequired() != null && seeker.getEducation().equals(jc.getEducationRequired())) {
        score += 10;
      }
      if (seeker != null && seeker.getCurrentCity() != null && seeker.getCurrentCity().equalsIgnoreCase(jc.getCity())) {
        score += 5;
      }
      if (score > 100) score = 100;
      if (score < 0) score = 0;
      item.setMatchScore(score);

      items.add(item);
    }

    InternJobRecommendationsDTO resp = new InternJobRecommendationsDTO();
    resp.setJobs(items);
    return Result.success("获取实习岗位推荐成功", resp);
  }

  @GetMapping("/job-position/{jobId}")
  @Operation(summary = "获取面向求职者的岗位详情", description = "返回岗位详情（包含公司、HR、申请状态等），供求职者端展示")
  public Result<SeekerJobPositionDTO> getJobPosition(@PathVariable Long jobId) {
    if (jobId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    // 获取岗位基础 DTO（包含技能等）
    JobInfoListDTO jobDto = jobInfoService.getJobInfoById(jobId);
    if (jobDto == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    // 获取完整实体以便取 companyId/hrId
    JobInfo jobModel = hrApi.getJobInfoById(jobId);
    if (jobModel == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    // 获取公司信息
    Company company = hrApi.getCompanyById(jobModel.getCompanyId());

    // 获取 hr info + avatar
    HrInfo hrInfo = hrApi.getHrInfoById(jobModel.getHrId());
    User hrUser = null;
    if (hrInfo != null) {
      hrUser = userAuthApi.getUserById(hrInfo.getUserId());
    }

    // 构建返回 DTO
    SeekerJobPositionDTO resp = new SeekerJobPositionDTO();
    resp.setJobId(jobModel.getId());
    resp.setJobTitle(jobDto.getJobTitle());
    resp.setJobCategory(jobDto.getJobCategory());
    resp.setDepartment(jobDto.getDepartment());
    resp.setCity(jobDto.getCity());
    resp.setAddress(jobModel.getAddress());
    resp.setSalaryMin(jobDto.getSalaryMin() == null ? 0 : jobDto.getSalaryMin().intValue());
    resp.setSalaryMax(jobDto.getSalaryMax() == null ? 0 : jobDto.getSalaryMax().intValue());
    resp.setSalaryMonths(jobDto.getSalaryMonths());
    resp.setEducationRequired(jobDto.getEducationRequired());
    resp.setJobType(jobDto.getJobType());
    resp.setExperienceRequired(jobDto.getExperienceRequired());
    resp.setDescription(jobModel.getDescription());
    resp.setResponsibilities(jobModel.getResponsibilities());
    resp.setRequirements(jobModel.getRequirements());
    resp.setSkills(jobDto.getSkills());
    resp.setViewCount(jobDto.getViewCount());
    resp.setApplicationCount(jobDto.getApplicationCount());
    resp.setPublishedAt(jobDto.getPublishedAt());

    if (company != null) {
      SeekerJobPositionDTO.CompanyDTO c = new SeekerJobPositionDTO.CompanyDTO();
      c.setCompanyId(company.getId());
      c.setCompanyName(company.getCompanyName());
      c.setCompanyLogo(company.getLogoUrl());
      c.setCompanyScale(company.getCompanyScale());
      c.setFinancingStage(company.getFinancingStage());
      c.setIndustry(company.getIndustry());
      c.setDescription(company.getDescription());
      c.setMainBusiness(company.getMainBusiness());
      c.setWebsite(company.getWebsite());
      resp.setCompany(c);
    }

    if (hrInfo != null) {
      SeekerJobPositionDTO.HrDTO h = new SeekerJobPositionDTO.HrDTO();
      h.setHrId(hrInfo.getId());
      h.setRealName(hrInfo.getRealName());
      h.setPosition(hrInfo.getPosition());
      h.setAvatarUrl(hrUser == null ? null : hrUser.getAvatarUrl());
      resp.setHr(h);
    }

    // 检查当前用户是否已投递（使用 JWT 当前用户）
    Long currentUserId = userContext.getCurrentUserId();
    Long seekerId = null;
    if (currentUserId != null) {
      seekerId = seekerApi.getJobSeekerIdByUserId(currentUserId);
    }
    SeekerJobPositionDTO.ApplicationDTO appDto = new SeekerJobPositionDTO.ApplicationDTO();
    if (seekerId != null) {
      Application application =
          applicationMapper.selectOne(
              new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Application>()
                  .eq(Application::getJobId, jobId)
                  .eq(Application::getJobSeekerId, seekerId));
      if (application != null) {
        appDto.setHasApplied(true);
        appDto.setApplicationId(application.getId());
        appDto.setStatus(application.getStatus() == null ? null : application.getStatus().intValue());
        appDto.setAppliedAt(application.getCreatedAt());
        // 尝试从 chat_message 表查找与该 application 相关的会话 ID（取最新一条消息）
        ChatMessage cm =
            chatMessageMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getApplicationId, application.getId())
                    .orderByDesc(ChatMessage::getCreatedAt)
                    .last("LIMIT 1"));
        if (cm != null) {
          appDto.setConversationId(cm.getConversationId());
        }
      } else {
        appDto.setHasApplied(false);
      }
    } else {
      appDto.setHasApplied(false);
    }
    resp.setApplication(appDto);

    return Result.success("查询成功", resp);
  }
}
