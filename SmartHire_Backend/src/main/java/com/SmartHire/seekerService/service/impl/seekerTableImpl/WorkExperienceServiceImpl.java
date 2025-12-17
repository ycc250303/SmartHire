package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.seekerService.dto.seekerTableDto.WorkExperienceDTO;
import com.SmartHire.seekerService.mapper.JobSeekerMapper;
import com.SmartHire.seekerService.mapper.WorkExperienceMapper;
import com.SmartHire.seekerService.model.JobSeeker;
import com.SmartHire.seekerService.model.WorkExperience;
import com.SmartHire.seekerService.service.JobSeekerService;
import com.SmartHire.seekerService.service.seekerTableService.WorkExperienceService;
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
 * 工作经历表 服务实现类
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class WorkExperienceServiceImpl extends ServiceImpl<WorkExperienceMapper, WorkExperience>
        implements WorkExperienceService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final Integer MAX_WORK_EXPERIENCE_COUNT = 5;

    @Autowired
    private WorkExperienceMapper workExperienceMapper;

    @Autowired
    private JobSeekerService jobSeekerService;

    @Autowired
    private JobSeekerMapper jobSeekerMapper;

    /**
     * 添加工作/实习经历
     *
     * @param request 工作经历信息
     */
    @Override
    public void addWorkExperience(@Valid WorkExperienceDTO request) {
        Long jobSeekerId = jobSeekerService.getJobSeekerId();

        long workExperienceCount = lambdaQuery().eq(WorkExperience::getJobSeekerId, jobSeekerId).count();

        if (workExperienceCount >= MAX_WORK_EXPERIENCE_COUNT) {
            throw new BusinessException(ErrorCode.WORK_EXPERIENCE_LIMIT_EXCEEDED);
        }

        WorkExperience experience = new WorkExperience();
        experience.setJobSeekerId(jobSeekerId);
        populateForCreate(experience, request);
        ensureChronology(experience.getStartDate(), experience.getEndDate());

        Date now = new Date();
        experience.setCreatedAt(now);
        experience.setUpdatedAt(now);

        workExperienceMapper.insert(experience);

        // 如果是实习经历，并且用户之前没有实习经历，则更新求职者的实习经历状态
        if (request.getIsInternship()) {
            // 检查用户之前是否有实习经历
            long internshipCount = lambdaQuery()
                    .eq(WorkExperience::getJobSeekerId, jobSeekerId)
                    .eq(WorkExperience::getIsInternship, 1)
                    .count();

            // 如果这是第一条实习经历，更新求职者的实习经历状态
            if (internshipCount == 1) {
                JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
                if (jobSeeker != null && !jobSeeker.isInternshipExperience()) {
                    jobSeeker.setInternshipExperience(true);
                    jobSeeker.setUpdatedAt(new Date());
                    jobSeekerMapper.updateById(jobSeeker);
                    log.info("更新求职者实习经历状态为有实习经历，求职者ID：{}", jobSeekerId);
                }
            }
        }
    }

    /**
     * 更新工作/实习经历
     *
     * @param id      工作/实习经历ID
     * @param request 工作/实习经历信息
     */
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
        WorkExperience experience = getOwnedExperience(id, jobSeekerId);

        workExperienceMapper.deleteById(id);

        // 如果删除的是实习经历，检查是否还有其他实习经历
        if (experience.getIsInternship()) {
            long remainingInternshipCount = lambdaQuery()
                    .eq(WorkExperience::getJobSeekerId, jobSeekerId)
                    .eq(WorkExperience::getIsInternship, 1)
                    .count();

            // 如果没有其他实习经历，更新求职者的实习经历状态为无
            if (remainingInternshipCount == 0) {
                JobSeeker jobSeeker = jobSeekerMapper.selectById(jobSeekerId);
                if (jobSeeker != null && jobSeeker.isInternshipExperience()) {
                    jobSeeker.setInternshipExperience(false);
                    jobSeeker.setUpdatedAt(new Date());
                    jobSeekerMapper.updateById(jobSeeker);
                    log.info("更新求职者实习经历状态为无实习经历，求职者ID：{}", jobSeekerId);
                }
            }
        }
    }

    private void populateForCreate(WorkExperience target, WorkExperienceDTO source) {
        target.setCompanyName(source.getCompanyName());
        target.setPosition(source.getPosition());
        target.setDepartment(source.getDepartment());
        target.setDescription(source.getDescription());
        target.setAchievements(source.getAchievements());
        target.setIsInternship(source.getIsInternship());
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
            target.setIsInternship(source.getIsInternship());
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
        dto.setIsInternship(experience.getIsInternship());
        dto.setStartMonth(formatYearMonth(experience.getStartDate()));
        dto.setEndMonth(
                experience.getEndDate() == null
                        ? WorkExperienceDTO.PRESENT
                        : formatYearMonth(experience.getEndDate()));
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
