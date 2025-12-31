package com.SmartHire.seekerService.service.impl;

import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.common.dto.hrDto.JobCardDTO;
import com.SmartHire.seekerService.dto.JobFavoriteDTO;
import com.SmartHire.seekerService.mapper.JobFavoriteMapper;
import com.SmartHire.seekerService.model.JobFavorite;
import com.SmartHire.seekerService.service.JobFavoriteService;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 职位收藏表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class JobFavoriteServiceImpl extends ServiceImpl<JobFavoriteMapper, JobFavorite>
    implements JobFavoriteService {

  @Autowired private JobSeekerService jobSeekerService;

  @Autowired private HrApi hrApi;

  @Override
  public void addJobFavorite(Long jobId) {
    if (jobId == null || jobId <= 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    // 验证岗位是否存在
    JobCardDTO jobCard = hrApi.getJobCardByJobId(jobId);
    if (jobCard == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    Long jobSeekerId = jobSeekerService.getJobSeekerId();

    // 检查是否已收藏
    long count =
        lambdaQuery()
            .eq(JobFavorite::getJobSeekerId, jobSeekerId)
            .eq(JobFavorite::getJobId, jobId)
            .count();
    if (count > 0) {
      throw new BusinessException(ErrorCode.JOB_FAVORITE_ALREADY_EXISTS);
    }

    // 添加收藏
    JobFavorite jobFavorite = new JobFavorite();
    jobFavorite.setJobSeekerId(jobSeekerId);
    jobFavorite.setJobId(jobId);
    jobFavorite.setCreatedAt(new Date());
    save(jobFavorite);
  }

  @Override
  public void removeJobFavorite(Long jobId) {
    if (jobId == null || jobId <= 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    Long jobSeekerId = jobSeekerService.getJobSeekerId();

    // 查找收藏记录
    JobFavorite jobFavorite =
        lambdaQuery()
            .eq(JobFavorite::getJobSeekerId, jobSeekerId)
            .eq(JobFavorite::getJobId, jobId)
            .one();
    if (jobFavorite == null) {
      throw new BusinessException(ErrorCode.JOB_FAVORITE_NOT_EXIST);
    }

    // 删除收藏
    removeById(jobFavorite.getId());
  }

  @Override
  public List<JobFavoriteDTO> getJobFavoriteList() {
    Long jobSeekerId = jobSeekerService.getJobSeekerId();

    // 查询所有收藏记录
    List<JobFavorite> favorites =
        lambdaQuery()
            .eq(JobFavorite::getJobSeekerId, jobSeekerId)
            .orderByDesc(JobFavorite::getCreatedAt)
            .list();

    // 转换为DTO并填充岗位信息
    return favorites.stream()
        .map(
            favorite -> {
              JobCardDTO jobCard = hrApi.getJobCardByJobId(favorite.getJobId());
              if (jobCard == null) {
                // 如果岗位不存在，跳过该记录
                return null;
              }

              JobFavoriteDTO dto = new JobFavoriteDTO();
              dto.setFavoriteId(favorite.getId());
              dto.setJobId(favorite.getJobId());
              dto.setCreatedAt(favorite.getCreatedAt());
              dto.setJobTitle(jobCard.getJobTitle());
              dto.setSalaryMin(jobCard.getSalaryMin());
              dto.setSalaryMax(jobCard.getSalaryMax());
              dto.setAddress(jobCard.getAddress());
              dto.setCompanyName(jobCard.getCompanyName());
              dto.setCompanyScale(jobCard.getCompanyScale());
              dto.setFinancingStage(jobCard.getFinancingStage());
              dto.setHrName(jobCard.getHrName());
              dto.setHrAvatarUrl(jobCard.getHrAvatarUrl());
              dto.setEducationRequired(jobCard.getEducationRequired());
              return dto;
            })
        .filter(dto -> dto != null)
        .collect(Collectors.toList());
  }
}
