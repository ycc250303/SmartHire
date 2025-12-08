package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.ProjectExperienceDTO;
import com.SmartHire.seekerService.mapper.ProjectExperienceMapper;
import com.SmartHire.seekerService.model.ProjectExperience;
import com.SmartHire.seekerService.service.seekerTableService.ProjectExperienceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 项目经历表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class ProjectExperienceServiceImpl
    extends AbstractSeekerOwnedService<ProjectExperienceMapper, ProjectExperience>
    implements ProjectExperienceService {

  private static final DateTimeFormatter YEAR_MONTH_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM");

  private static final Integer MAX_PROJECT_EXPERIENCES = 5;
  @Autowired private ProjectExperienceMapper projectExperienceMapper;

  /**
   * 添加项目经历
   *
   * @param request 项目经历信息
   */
  @Override
  public void addProjectExperience(@Valid ProjectExperienceDTO request) {
    Long jobSeekerId = currentSeekerId();

    long projectExperienceCount =
        lambdaQuery().eq(ProjectExperience::getJobSeekerId, jobSeekerId).count();
    if (projectExperienceCount >= MAX_PROJECT_EXPERIENCES) {
      throw new BusinessException(ErrorCode.PROJECT_EXPERIENCE_LIMIT_EXCEEDED);
    }

    ProjectExperience experience = new ProjectExperience();
    experience.setJobSeekerId(jobSeekerId);
    populateProjectForCreate(experience, request);
    ensureChronology(experience.getStartDate(), experience.getEndDate());

    Date now = new Date();
    experience.setCreatedAt(now);
    experience.setUpdatedAt(now);

    projectExperienceMapper.insert(experience);
  }

  /**
   * 更新项目经历
   *
   * @param id 项目经历ID
   * @param request 项目经历信息
   */
  @Override
  public void updateProjectExperience(Long id, ProjectExperienceDTO request) {
    ProjectExperience existingExperience = getOwnedProjectExperience(id);

    if (!applyProjectUpdates(existingExperience, request)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    ensureChronology(existingExperience.getStartDate(), existingExperience.getEndDate());

    existingExperience.setUpdatedAt(new Date());
    projectExperienceMapper.updateById(existingExperience);
  }

  /**
   * 获取项目经历列表
   *
   * @return 项目经历列表
   */
  @Override
  public List<ProjectExperienceDTO> getProjectExperiences() {
    Long jobSeekerId = currentSeekerId();
    List<ProjectExperience> experiences =
        lambdaQuery()
            .eq(ProjectExperience::getJobSeekerId, jobSeekerId)
            .orderByDesc(ProjectExperience::getStartDate)
            .list();

    return experiences.stream().map(this::toDto).collect(Collectors.toList());
  }

  /**
   * 删除项目经历
   *
   * @param id 项目经历ID
   */
  @Override
  public void deleteProjectExperience(Long id) {
    getOwnedProjectExperience(id);
    projectExperienceMapper.deleteById(id);
  }

  /**
   * 填充项目信息
   *
   * @param target 目标项目对象
   * @param source 源项目信息
   */
  private void populateProjectForCreate(ProjectExperience target, ProjectExperienceDTO source) {
    target.setProjectName(source.getProjectName());
    target.setProjectRole(source.getProjectRole());
    target.setDescription(source.getDescription());
    target.setResponsibility(source.getResponsibility());
    target.setAchievement(source.getAchievement());
    target.setProjectUrl(source.getProjectUrl());
    target.setStartDate(parseYearMonth(source.getStartMonth()));
    target.setEndDate(parseOptionalYearMonth(source.getEndMonth()));
  }

  /**
   * 应用项目更新
   *
   * @param target 源项目对象
   * @param source 源项目信息
   * @return 是否有更新
   */
  private boolean applyProjectUpdates(ProjectExperience target, ProjectExperienceDTO source) {
    boolean hasUpdate = false;
    if (StringUtils.hasText(source.getProjectName())) {
      target.setProjectName(source.getProjectName());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getProjectRole())) {
      target.setProjectRole(source.getProjectRole());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getDescription())) {
      target.setDescription(source.getDescription());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getResponsibility())) {
      target.setResponsibility(source.getResponsibility());
      hasUpdate = true;
    }
    if (source.getAchievement() != null) {
      target.setAchievement(source.getAchievement());
      hasUpdate = true;
    }
    if (source.getProjectUrl() != null) {
      target.setProjectUrl(source.getProjectUrl());
      hasUpdate = true;
    }
    if (StringUtils.hasText(source.getStartMonth())) {
      target.setStartDate(parseYearMonth(source.getStartMonth()));
      hasUpdate = true;
    }
    if (source.getEndMonth() != null) {
      target.setEndDate(parseOptionalYearMonth(source.getEndMonth()));
      hasUpdate = true;
    }
    return hasUpdate;
  }

  /**
   * 解析年月字符串
   *
   * @param yearMonth 年月字符串
   * @return 解析后的LocalDate对象
   */
  private LocalDate parseYearMonth(String yearMonth) {
    try {
      return YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER).atDay(1);
    } catch (DateTimeParseException ex) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
  }

  /**
   * 解析可选年月字符串
   *
   * @param yearMonth 可选年月字符串
   * @return 解析后的LocalDate对象
   */
  private LocalDate parseOptionalYearMonth(String yearMonth) {
    if (!StringUtils.hasText(yearMonth) || ProjectExperienceDTO.PRESENT.equals(yearMonth)) {
      return null;
    }
    return parseYearMonth(yearMonth);
  }

  private void ensureChronology(LocalDate start, LocalDate end) {
    if (start == null) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
    if (end != null && end.isBefore(start)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }
  }

  private ProjectExperienceDTO toDto(ProjectExperience experience) {
    ProjectExperienceDTO dto = new ProjectExperienceDTO();
    dto.setId(experience.getId());
    dto.setProjectName(experience.getProjectName());
    dto.setProjectRole(experience.getProjectRole());
    dto.setDescription(experience.getDescription());
    dto.setResponsibility(experience.getResponsibility());
    dto.setAchievement(experience.getAchievement());
    dto.setProjectUrl(experience.getProjectUrl());
    dto.setStartMonth(formatYearMonth(experience.getStartDate()));
    dto.setEndMonth(
        experience.getEndDate() == null
            ? ProjectExperienceDTO.PRESENT
            : formatYearMonth(experience.getEndDate()));
    return dto;
  }

  private String formatYearMonth(LocalDate date) {
    if (date == null) {
      return null;
    }
    return YEAR_MONTH_FORMATTER.format(YearMonth.from(date));
  }

  private ProjectExperience getOwnedProjectExperience(Long id) {
    return requireOwnedEntity(
        id,
        projectExperienceMapper::selectById,
        ProjectExperience::getJobSeekerId,
        ErrorCode.PROJECT_EXPERIENCE_NOT_EXIST,
        ErrorCode.PROJECT_EXPERIENCE_NOT_BELONG_TO_USER);
  }
}
