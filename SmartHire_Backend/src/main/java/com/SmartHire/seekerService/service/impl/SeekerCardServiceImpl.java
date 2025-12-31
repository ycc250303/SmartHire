package com.SmartHire.seekerService.service.impl;

import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.common.dto.userDto.UserCommonDTO;
import com.SmartHire.common.dto.seekerDto.SeekerCardDTO;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.mapper.EducationExperienceMapper;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.model.EducationExperience;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.service.SeekerCardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 求职卡片查询服务实现。 */
@Slf4j
@Service
public class SeekerCardServiceImpl implements SeekerCardService {
  private static final Map<Integer, String> EDUCATION_LABEL = Map.ofEntries(
      Map.entry(0, "高中"),
      Map.entry(1, "大专"),
      Map.entry(2, "本科"),
      Map.entry(3, "硕士"),
      Map.entry(4, "博士"));

  @Autowired
  private UserAuthApi userAuthApi;

  @Autowired
  private JobSeekerMapper jobSeekerMapper;

  @Autowired
  private EducationExperienceMapper educationExperienceMapper;

  @Override
  public SeekerCardDTO getJobCard(Long userId) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    try {
      UserCommonDTO user = userAuthApi.getUserById(userId);

      JobSeeker jobSeeker = jobSeekerMapper.selectOne(
          new LambdaQueryWrapper<JobSeeker>().eq(JobSeeker::getUserId, userId));
      if (jobSeeker == null) {
        throw new BusinessException(ErrorCode.SEEKER_NOT_EXIST);
      }

      EducationExperience highestEducation = findHighestEducation(jobSeeker.getId());
      SeekerCardDTO dto = new SeekerCardDTO();
      dto.setUsername(user.getUsername());
      dto.setAge(calculateAge(jobSeeker.getBirthDate()));
      dto.setJobStatus(jobSeeker.getJobStatus());

      if (highestEducation != null) {
        dto.setMajor(highestEducation.getMajor());
        dto.setGraduationYear(formatGraduationYear(highestEducation.getEndYear()));
        dto.setHighestEducation(resolveEducationName(highestEducation.getEducation()));
      } else {
        dto.setMajor(null);
        dto.setGraduationYear(null);
        dto.setHighestEducation(resolveEducationName(jobSeeker.getEducation()));
      }

      return dto;
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("获取求职卡片失败, userId={}", userId, ex);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
  }

  private EducationExperience findHighestEducation(Long jobSeekerId) {
    if (jobSeekerId == null) {
      return null;
    }
    List<EducationExperience> educationExperiences = educationExperienceMapper.selectList(
        new LambdaQueryWrapper<EducationExperience>()
            .eq(EducationExperience::getJobSeekerId, jobSeekerId)
            .orderByDesc(EducationExperience::getEducation)
            .orderByDesc(EducationExperience::getEndYear));
    return educationExperiences.isEmpty() ? null : educationExperiences.get(0);
  }

  private Integer calculateAge(java.util.Date birthDate) {
    if (birthDate == null) {
      return null;
    }
    LocalDate birth = Instant.ofEpochMilli(birthDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate today = LocalDate.now();
    if (birth.isAfter(today)) {
      return null;
    }
    return Period.between(birth, today).getYears();
  }

  private String formatGraduationYear(LocalDate endYear) {
    return Optional.ofNullable(endYear)
        .map(LocalDate::getYear)
        .map(year -> year + "年应届生")
        .orElse(null);
  }

  private String resolveEducationName(Integer education) {
    return education == null ? "未知" : EDUCATION_LABEL.getOrDefault(education, "未知");
  }

  @Override
  public List<SeekerCardDTO> getAllSeekerCards(Integer pageNum, Integer pageSize) {
    // 计算偏移量
    Integer offset = (pageNum - 1) * pageSize;
    return jobSeekerMapper.getAllSeekerCardsByPage(offset, pageSize);
  }

  @Override
  public List<SeekerCardDTO> getSeekersByMultipleConditions(
      String city, Integer education, Double salaryMin, Double salaryMax,
      Integer isInternship, Integer jobStatus, Integer hasInternship) {
    return jobSeekerMapper.getSeekerCardsByMultipleConditions(
        city, education, salaryMin, salaryMax, isInternship, jobStatus, hasInternship);
  }
}
