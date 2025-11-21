package com.SmartHire.seekerService.service.impl;

import com.SmartHire.seekerService.dto.WorkExperienceDTO;
import com.SmartHire.seekerService.mapper.WorkExperienceMapper;
import com.SmartHire.seekerService.model.WorkExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.WorkExperienceService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 工作经历表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class WorkExperienceServiceImpl extends ServiceImpl<WorkExperienceMapper, WorkExperience>
        implements WorkExperienceService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private WorkExperienceMapper workExperienceMapper;

    @Autowired
    private JobSeekerService jobSeekerService;

    @Override
    public void addWorkExperience(@Valid WorkExperienceDTO request) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();

        WorkExperience experience = new WorkExperience();
        experience.setJobSeekerId(jobSeekerId);
        populateForCreate(experience, request);
        ensureChronology(experience.getStartDate(), experience.getEndDate());

        Date now = new Date();
        experience.setCreatedAt(now);
        experience.setUpdatedAt(now);

        workExperienceMapper.insert(experience);
    }

    @Override
    public void updateWorkExperience(Long id, WorkExperienceDTO request) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
        WorkExperience existingExperience = getOwnedExperience(id, jobSeekerId);

        if (!applyUpdates(existingExperience, request)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        ensureChronology(existingExperience.getStartDate(), existingExperience.getEndDate());

        existingExperience.setUpdatedAt(new Date());
        workExperienceMapper.updateById(existingExperience);
    }

    @Override
    public List<WorkExperienceDTO> getWorkExperiences() {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
        return lambdaQuery()
                .eq(WorkExperience::getJobSeekerId, jobSeekerId)
                .orderByDesc(WorkExperience::getStartDate)
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteWorkExperience(Long id) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();
        getOwnedExperience(id, jobSeekerId);
        workExperienceMapper.deleteById(id);
    }

    private void populateForCreate(WorkExperience target, WorkExperienceDTO source) {
        target.setCompanyName(source.getCompanyName());
        target.setPosition(source.getPosition());
        target.setDepartment(source.getDepartment());
        target.setDescription(source.getDescription());
        target.setAchievements(source.getAchievements());
        target.setIsInternship(source.getIsInternship() == null ? null : source.getIsInternship().byteValue());
        target.setStartDate(parseYearMonth(source.getStartMonth()));
        target.setEndDate(parseOptionalYearMonth(source.getEndMonth()));
    }

    private boolean applyUpdates(WorkExperience target, WorkExperienceDTO source) {
        boolean hasUpdate = false;
        if (StringUtils.hasText(source.getCompanyName())) {
            target.setCompanyName(source.getCompanyName());
            hasUpdate = true;
        }
        if (StringUtils.hasText(source.getPosition())) {
            target.setPosition(source.getPosition());
            hasUpdate = true;
        }
        if (source.getDepartment() != null) {
            target.setDepartment(source.getDepartment());
            hasUpdate = true;
        }
        if (StringUtils.hasText(source.getDescription())) {
            target.setDescription(source.getDescription());
            hasUpdate = true;
        }
        if (StringUtils.hasText(source.getAchievements())) {
            target.setAchievements(source.getAchievements());
            hasUpdate = true;
        }
        if (source.getIsInternship() != null) {
            target.setIsInternship(source.getIsInternship().byteValue());
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

    private Date parseYearMonth(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
            LocalDate localDate = ym.atDay(1);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private Date parseOptionalYearMonth(String yearMonth) {
        if (!StringUtils.hasText(yearMonth) || WorkExperienceDTO.PRESENT.equals(yearMonth)) {
            return null;
        }
        return parseYearMonth(yearMonth);
    }

    private String formatYearMonth(Date date) {
        if (date == null) {
            return null;
        }
        YearMonth ym = YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return YEAR_MONTH_FORMATTER.format(ym);
    }

    private void ensureChronology(Date start, Date end) {
        if (start == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (end != null && end.before(start)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private WorkExperienceDTO toDto(WorkExperience experience) {
        WorkExperienceDTO dto = new WorkExperienceDTO();
        dto.setId(experience.getId());
        dto.setCompanyName(experience.getCompanyName());
        dto.setPosition(experience.getPosition());
        dto.setDepartment(experience.getDepartment());
        dto.setDescription(experience.getDescription());
        dto.setAchievements(experience.getAchievements());
        dto.setIsInternship(experience.getIsInternship() == null ? null : experience.getIsInternship().intValue());
        dto.setStartMonth(formatYearMonth(experience.getStartDate()));
        dto.setEndMonth(
                experience.getEndDate() == null ? WorkExperienceDTO.PRESENT : formatYearMonth(experience.getEndDate()));
        return dto;
    }

    private WorkExperience getOwnedExperience(Long id, Long jobSeekerId) {
        WorkExperience experience = workExperienceMapper.selectById(id);
        if (experience == null) {
            throw new BusinessException(ErrorCode.WORK_EXPERIENCE_NOT_EXIST);
        }
        if (!Objects.equals(experience.getJobSeekerId(), jobSeekerId)) {
            throw new BusinessException(ErrorCode.WORK_EXPERIENCE_NOT_BELONG_TO_USER);
        }
        return experience;
    }
}
