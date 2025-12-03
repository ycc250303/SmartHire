package com.SmartHire.seekerService.service.impl;

import com.SmartHire.common.api.SeekerApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.mapper.ResumeMapper;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.Resume;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 求职者服务API实现类 用于模块间通信 */
@Service
public class SeekerApiImpl implements SeekerApi {

  @Autowired private JobSeekerMapper jobSeekerMapper;

  @Autowired private ResumeMapper resumeMapper;

  @Override
  public Long getJobSeekerIdByUserId(Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    JobSeeker jobSeeker =
        jobSeekerMapper.selectOne(
            new LambdaQueryWrapper<JobSeeker>().eq(JobSeeker::getUserId, userId));
    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }
    return jobSeeker.getId();
  }

  @Override
  public JobSeeker getJobSeekerById(Long jobSeekerId) {
    if (jobSeekerId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
    if (jobSeeker == null) {
      throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
    }
    return jobSeeker;
  }

  @Override
  public Resume getResumeById(Long resumeId) {
    if (resumeId == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    Resume resume = resumeMapper.selectById(resumeId);
    if (resume == null) {
      throw new BusinessException(ErrorCode.RESUME_NOT_EXIST);
    }
    return resume;
  }

  @Override
  public boolean validateResumeOwnership(Long resumeId, Long jobSeekerId) {
    if (resumeId == null || jobSeekerId == null) {
      return false;
    }
    Resume resume = resumeMapper.selectById(resumeId);
    return resume != null && jobSeekerId.equals(resume.getJobSeekerId());
  }

  @Autowired private com.SmartHire.seekerService.service.JobSeekerService jobSeekerService;

  @Override
  public void deleteJobSeekerByUserId(Long userId) {
    if (userId == null) {
      return;
    }
    jobSeekerService.deleteJobSeekerByUserId(userId);
  }
}
