package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.EducationExperienceDTO;
import com.SmartHire.seekerService.mapper.EducationExperienceMapper;
import com.SmartHire.seekerService.model.EducationExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.seekerTableService.EducationExperienceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 教育经历表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class EducationExperienceServiceImpl
    extends AbstractSeekerOwnedService<EducationExperienceMapper, EducationExperience>
    implements EducationExperienceService {
  @Autowired private EducationExperienceMapper educationExperienceMapper;

  @Autowired private JobSeekerService jobSeekerService;

  /**
   * 添加教育经历
   *
   * @param request
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void addEducationExperience(@Valid EducationExperienceDTO request) {
    Long jobSeekerId = currentSeekerId();

    EducationExperience educationExperience = new EducationExperience();
    educationExperience.setJobSeekerId(jobSeekerId);
    populateEducationForCreate(educationExperience, request);

    if (isDuplicateEducation(
        jobSeekerId,
        educationExperience.getSchoolName(),
        educationExperience.getMajor(),
        educationExperience.getEducation(),
        educationExperience.getStartYear(),
        educationExperience.getEndYear(),
        null)) {
      throw new BusinessException(ErrorCode.EDUCATION_EXPERIENCE_ALREADY_EXIST);
    }

    educationExperience.setCreatedAt(new Date());
    educationExperience.setUpdatedAt(new Date());

    educationExperienceMapper.insert(educationExperience);

    // 自动更新求职者的最高学历和毕业年份
    if (jobSeekerService instanceof com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) {
      ((com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) jobSeekerService)
          .updateEducationAndGraduationYear(jobSeekerId);
    }
  }

  /**
   * 修改教育经历
   *
   * @param id
   * @param request
   */
  @Override
  public void updateEducationExperience(Long id, EducationExperienceDTO request) {
    // 获取当前用户的求职者ID
    Long jobSeekerId = currentSeekerId();

    // 查询教育经历是否存在且属于当前用户
    EducationExperience existingExperience = getOwnedEducation(id);

    if (!applyEducationUpdates(existingExperience, request)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    // 更新修改时间
    existingExperience.setUpdatedAt(new Date());

    if (isDuplicateEducation(
        jobSeekerId,
        existingExperience.getSchoolName(),
        existingExperience.getMajor(),
        existingExperience.getEducation(),
        existingExperience.getStartYear(),
        existingExperience.getEndYear(),
        existingExperience.getId())) {
      throw new BusinessException(ErrorCode.EDUCATION_EXPERIENCE_ALREADY_EXIST);
    }

    // 执行更新
    educationExperienceMapper.updateById(existingExperience);

    // 自动更新求职者的最高学历和毕业年份
    if (jobSeekerService instanceof com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) {
      ((com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) jobSeekerService)
          .updateEducationAndGraduationYear(jobSeekerId);
    }
  }

  /**
   * 获取教育经历列表
   *
   * @return List<EducationExperience>
   */
  @Override
  public List<EducationExperience> getEducationExperiences() {
    // 获取当前用户的求职者ID
    Long jobSeekerId = currentSeekerId();

    // 查询该求职者的所有教育经历
    return lambdaQuery()
        .eq(EducationExperience::getJobSeekerId, jobSeekerId)
        .orderByDesc(EducationExperience::getStartYear)
        .list();
  }

  /**
   * 删除教育经历
   *
   * @param id
   */
  @Override
  public void deleteEducationExperience(Long id) {
    EducationExperience educationExperience = getOwnedEducation(id);
    Long jobSeekerId = educationExperience.getJobSeekerId();

    educationExperienceMapper.deleteById(id);

    // 自动更新求职者的最高学历和毕业年份
    if (jobSeekerService instanceof com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) {
      ((com.SmartHire.seekerService.service.impl.JobSeekerServiceImpl) jobSeekerService)
          .updateEducationAndGraduationYear(jobSeekerId);
    }
  }

  /**
   * 填充教育经历信息
   *
   * @param target
   * @param source
   */
  private void populateEducationForCreate(
      EducationExperience target, EducationExperienceDTO source) {
    target.setSchoolName(source.getSchoolName());
    target.setMajor(source.getMajor());
    target.setEducation(source.getEducation());
    target.setStartYear(parseYear(source.getStartYear()));
    target.setEndYear(parseYear(source.getEndYear()));
  }

  /**
   * 应用教育经历更新
   *
   * @param target
   * @param source
   * @return
   */
  private boolean applyEducationUpdates(EducationExperience target, EducationExperienceDTO source) {
    boolean hasUpdate = false;

    if (StringUtils.hasText(source.getSchoolName())) {
      target.setSchoolName(source.getSchoolName());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getMajor())) {
      target.setMajor(source.getMajor());
      hasUpdate = true;
    }
    if (source.getEducation() != null) {
      target.setEducation(source.getEducation());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getStartYear())) {
      target.setStartYear(parseYear(source.getStartYear()));
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getEndYear())) {
      target.setEndYear(parseYear(source.getEndYear()));
      hasUpdate = true;
    }

    return hasUpdate;
  }

  /**
   * 解析年份字符串为LocalDate对象
   *
   * @param year
   * @return
   */
  private LocalDate parseYear(String year) {
    return LocalDate.parse(year + "-01-01");
  }

  /**
   * 检查教育经历是否重复
   *
   * @param jobSeekerId
   * @param schoolName
   * @param major
   * @param education
   * @param startYear
   * @param endYear
   * @param excludeId
   * @return
   */
  private boolean isDuplicateEducation(
      Long jobSeekerId,
      String schoolName,
      String major,
      Integer education,
      LocalDate startYear,
      LocalDate endYear,
      Long excludeId) {
    return lambdaQuery()
            .eq(EducationExperience::getJobSeekerId, jobSeekerId)
            .eq(EducationExperience::getSchoolName, schoolName)
            .eq(EducationExperience::getMajor, major)
            .eq(EducationExperience::getEducation, education)
            .eq(EducationExperience::getStartYear, startYear)
            .eq(EducationExperience::getEndYear, endYear)
            .ne(excludeId != null, EducationExperience::getId, excludeId)
            .count()
        > 0;
  }

  private EducationExperience getOwnedEducation(Long id) {
    return requireOwnedEntity(
        id,
        educationExperienceMapper::selectById,
        EducationExperience::getJobSeekerId,
        ErrorCode.EDUCATION_EXPERIENCE_NOT_EXIST,
        ErrorCode.EDUCATION_EXPERIENCE_NOT_BELONG_TO_USER);
  }
}
