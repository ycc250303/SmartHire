package com.SmartHire.seekerService.service.impl;

import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.mapper.*;
import com.SmartHire.seekerService.model.*;
import com.SmartHire.seekerService.service.OnlineResumeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 在线简历聚合查询服务实现。 */
@Slf4j
@Service
public class OnlineResumeServiceImpl implements OnlineResumeService {

  @Autowired
  private UserAuthApi userAuthApi;

  @Autowired
  private JobSeekerMapper jobSeekerMapper;

  @Autowired
  private EducationExperienceMapper educationExperienceMapper;

  @Autowired
  private JobSeekerExpectationMapper jobSeekerExpectationMapper;

  @Autowired
  private ProjectExperienceMapper projectExperienceMapper;

  @Autowired
  private WorkExperienceMapper workExperienceMapper;

  @Autowired
  private SkillMapper skillMapper;

  @Override
  public Map<String, Object> getOnlineResumeByUserId(Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    try {
      // 1. 获取基础用户信息
      UserCommonDTO user = userAuthApi.getUserById(userId);

      // 2. 获取求职者扩展信息
      JobSeeker jobSeeker = jobSeekerMapper.selectOne(
          new LambdaQueryWrapper<JobSeeker>().eq(JobSeeker::getUserId, userId));
      if (jobSeeker == null) {
        throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
      }
      Long jobSeekerId = jobSeeker.getId();

      // 3. 并行获取其他模块信息（暂时串行实现）
      List<EducationExperience> educationList = educationExperienceMapper.selectList(
          new LambdaQueryWrapper<EducationExperience>()
              .eq(EducationExperience::getJobSeekerId, jobSeekerId)
              .orderByDesc(EducationExperience::getEndYear));

      List<JobSeekerExpectation> expectationList = jobSeekerExpectationMapper.selectList(
          new LambdaQueryWrapper<JobSeekerExpectation>().eq(JobSeekerExpectation::getJobSeekerId, jobSeekerId));

      List<ProjectExperience> projectList = projectExperienceMapper.selectList(
          new LambdaQueryWrapper<ProjectExperience>()
              .eq(ProjectExperience::getJobSeekerId, jobSeekerId)
              .orderByDesc(ProjectExperience::getEndDate));

      List<WorkExperience> workList = workExperienceMapper.selectList(
          new LambdaQueryWrapper<WorkExperience>()
              .eq(WorkExperience::getJobSeekerId, jobSeekerId)
              .orderByDesc(WorkExperience::getEndDate));

      List<Skill> skillList = skillMapper.selectList(
          new LambdaQueryWrapper<Skill>().eq(Skill::getJobSeekerId, jobSeekerId));

      // 4. 组装结果
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("user", user);
      result.put("seekerInfo", jobSeeker);
      result.put("educations", educationList);
      result.put("expectations", expectationList);
      result.put("projects", projectList);
      result.put("workExperiences", workList);
      result.put("skills", skillList);

      return result;
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("获取在线简历失败, userId={}", userId, ex);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
  }
}
