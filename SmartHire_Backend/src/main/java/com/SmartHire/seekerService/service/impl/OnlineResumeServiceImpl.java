package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.mapper.EducationExperienceMapper;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.mapper.ProjectExperienceMapper;
import com.SmartHire.seekerService.mapper.SkillMapper;
import com.SmartHire.seekerService.mapper.WorkExperienceMapper;
import com.SmartHire.seekerService.model.EducationExperience;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.ProjectExperience;
import com.SmartHire.seekerService.model.Skill;
import com.SmartHire.seekerService.model.WorkExperience;
import com.SmartHire.seekerService.service.OnlineResumeService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.userAuthService.mapper.UserAuthMapper;
import com.SmartHire.userAuthService.model.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 在线简历聚合查询服务实现。
 */
@Slf4j
@Service
public class OnlineResumeServiceImpl implements OnlineResumeService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private JobSeekerMapper jobSeekerMapper;

    @Autowired
    private EducationExperienceMapper educationExperienceMapper;

    @Autowired
    private ProjectExperienceMapper projectExperienceMapper;

    @Autowired
    private WorkExperienceMapper workExperienceMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Override
    public Map<String, Object> getOnlineResumeByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        try {
            User user = userAuthMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_ID_NOT_EXIST);
            }
            if (!Objects.equals(user.getUserType(), 1)) {
                throw new BusinessException(ErrorCode.USER_NOT_SEEKER);
            }

            JobSeeker jobSeeker = jobSeekerMapper.selectOne(
                    new LambdaQueryWrapper<JobSeeker>().eq(JobSeeker::getUserId, userId));
            if (jobSeeker == null) {
                throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
            }
            Long jobSeekerId = jobSeeker.getId();

            List<EducationExperience> educationExperiences = educationExperienceMapper.selectList(
                    new LambdaQueryWrapper<EducationExperience>()
                            .eq(EducationExperience::getJobSeekerId, jobSeekerId)
                            .orderByDesc(EducationExperience::getStartYear));

            List<ProjectExperience> projectExperiences = projectExperienceMapper.selectList(
                    new LambdaQueryWrapper<ProjectExperience>()
                            .eq(ProjectExperience::getJobSeekerId, jobSeekerId)
                            .orderByDesc(ProjectExperience::getStartDate));

            List<WorkExperience> workExperiences = workExperienceMapper.selectList(
                    new LambdaQueryWrapper<WorkExperience>()
                            .eq(WorkExperience::getJobSeekerId, jobSeekerId)
                            .orderByDesc(WorkExperience::getStartDate));

            List<Skill> skills = skillMapper.selectList(
                    new LambdaQueryWrapper<Skill>().eq(Skill::getJobSeekerId, jobSeekerId));

            Map<String, Object> resume = new LinkedHashMap<>();
            resume.put("userBase", buildUserBaseInfo(user));
            resume.put("educationExperiences", educationExperiences);
            resume.put("projectExperiences", projectExperiences);
            resume.put("workExperiences", workExperiences);
            resume.put("skills", skills);

            return resume;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("查询在线简历失败, userId={}", userId, ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private Map<String, Object> buildUserBaseInfo(User user) {
        Map<String, Object> userBase = new LinkedHashMap<>();
        userBase.put("id", user.getId());
        userBase.put("username", user.getUsername());
        userBase.put("avatarUrl", user.getAvatarUrl());
        return userBase;
    }
}
