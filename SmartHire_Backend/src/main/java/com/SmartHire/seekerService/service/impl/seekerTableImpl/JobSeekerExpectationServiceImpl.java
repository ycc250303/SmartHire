package com.SmartHire.seekerService.service.impl.seekerTableImpl;

import com.SmartHire.seekerService.dto.seekerTableDto.JobSeekerExpectationDTO;
import com.SmartHire.seekerService.model.JobSeekerExpectation;
import com.SmartHire.seekerService.mapper.JobSeekerExpectationMapper;
import com.SmartHire.seekerService.service.seekerTableService.JobSeekerExpectationService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 求职者期望表 服务实现类
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Slf4j
@Service
public class JobSeekerExpectationServiceImpl
        extends AbstractSeekerOwnedService<JobSeekerExpectationMapper, JobSeekerExpectation>
        implements JobSeekerExpectationService {

    @Autowired
    private JobSeekerExpectationMapper jobSeekerExpectationMapper;

    private static final int MAX_EXPECTATION_COUNT = 5;
    private static final BigDecimal MAX_SALARY = new BigDecimal("100000.00");

    /**
     * 添加求职期望
     *
     * @param request
     */
    @Override
    public void addJobSeekerExpectation(@Valid JobSeekerExpectationDTO request) {
        Long jobSeekerId = currentSeekerId();

        // 检查是否已达到上限（最多5个）
        long currentCount = lambdaQuery()
                .eq(JobSeekerExpectation::getJobSeekerId, jobSeekerId)
                .count();
        if (currentCount >= MAX_EXPECTATION_COUNT) {
            throw new BusinessException(ErrorCode.JOB_SEEKER_EXPECTATION_LIMIT_EXCEEDED);
        }

        // 校验薪资范围
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        JobSeekerExpectation expectation = new JobSeekerExpectation();
        expectation.setJobSeekerId(jobSeekerId);
        populateExpectationForCreate(expectation, request);

        expectation.setCreatedAt(new Date());
        expectation.setUpdatedAt(new Date());

        jobSeekerExpectationMapper.insert(expectation);
    }

    /**
     * 修改求职期望
     *
     * @param id
     * @param request
     */
    @Override
    public void updateJobSeekerExpectation(Long id, JobSeekerExpectationDTO request) {
        // 查询求职期望是否存在且属于当前用户
        JobSeekerExpectation existingExpectation = getOwnedExpectation(id);

        // 如果更新了薪资，需要校验
        BigDecimal newSalaryMin = request.getSalaryMin() != null ? request.getSalaryMin()
                : existingExpectation.getSalaryMin();
        BigDecimal newSalaryMax = request.getSalaryMax() != null ? request.getSalaryMax()
                : existingExpectation.getSalaryMax();
        validateSalaryRange(newSalaryMin, newSalaryMax);

        if (!applyExpectationUpdates(existingExpectation, request)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        // 更新修改时间
        existingExpectation.setUpdatedAt(new Date());

        // 执行更新
        jobSeekerExpectationMapper.updateById(existingExpectation);
    }

    /**
     * 获取求职期望列表
     *
     * @return List<JobSeekerExpectation>
     */
    @Override
    public List<JobSeekerExpectation> getJobSeekerExpectations() {
        // 获取当前用户的求职者ID
        Long jobSeekerId = currentSeekerId();

        // 查询该求职者的所有求职期望
        return lambdaQuery()
                .eq(JobSeekerExpectation::getJobSeekerId, jobSeekerId)
                .orderByDesc(JobSeekerExpectation::getCreatedAt)
                .list();
    }

    /**
     * 删除求职期望
     *
     * @param id
     */
    @Override
    public void deleteJobSeekerExpectation(Long id) {
        getOwnedExpectation(id);

        jobSeekerExpectationMapper.deleteById(id);
    }

    /**
     * 填充求职期望信息
     *
     * @param target
     * @param source
     */
    private void populateExpectationForCreate(JobSeekerExpectation target, JobSeekerExpectationDTO source) {
        target.setExpectedPosition(source.getExpectedPosition());
        // 期望行业为非必填项，默认为"不限行业"
        target.setExpectedIndustry(StringUtils.hasText(source.getExpectedIndustry())
                ? source.getExpectedIndustry()
                : "不限行业");
        target.setWorkCity(source.getWorkCity());
        target.setSalaryMin(source.getSalaryMin());
        target.setSalaryMax(source.getSalaryMax());
    }

    /**
     * 应用求职期望更新
     *
     * @param target
     * @param source
     * @return
     */
    private boolean applyExpectationUpdates(JobSeekerExpectation target, JobSeekerExpectationDTO source) {
        boolean hasUpdate = false;

        if (StringUtils.hasText(source.getExpectedPosition())) {
            target.setExpectedPosition(source.getExpectedPosition());
            hasUpdate = true;
        }
        if (source.getExpectedIndustry() != null) {
            // 如果传入空字符串，设置为"不限行业"
            target.setExpectedIndustry(StringUtils.hasText(source.getExpectedIndustry())
                    ? source.getExpectedIndustry()
                    : "不限行业");
            hasUpdate = true;
        }
        if (StringUtils.hasText(source.getWorkCity())) {
            target.setWorkCity(source.getWorkCity());
            hasUpdate = true;
        }
        if (source.getSalaryMin() != null) {
            target.setSalaryMin(source.getSalaryMin());
            hasUpdate = true;
        }
        if (source.getSalaryMax() != null) {
            target.setSalaryMax(source.getSalaryMax());
            hasUpdate = true;
        }

        return hasUpdate;
    }

    /**
     * 校验薪资范围
     *
     * @param salaryMin
     * @param salaryMax
     */
    private void validateSalaryRange(BigDecimal salaryMin, BigDecimal salaryMax) {
        if (salaryMin != null && salaryMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (salaryMax != null && salaryMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        // 校验最大值不能小于最小值
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0) {
            throw new BusinessException(ErrorCode.SALARY_MAX_LESS_THAN_MIN);
        }
        if (salaryMin != null && salaryMin.compareTo(MAX_SALARY) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (salaryMax != null && salaryMax.compareTo(MAX_SALARY) > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private JobSeekerExpectation getOwnedExpectation(Long id) {
        return requireOwnedEntity(id,
                jobSeekerExpectationMapper::selectById,
                JobSeekerExpectation::getJobSeekerId,
                ErrorCode.JOB_SEEKER_EXPECTATION_NOT_EXIST,
                ErrorCode.JOB_SEEKER_EXPECTATION_NOT_BELONG_TO_USER);
    }
}
