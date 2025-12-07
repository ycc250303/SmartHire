package com.SmartHire.hrService.service.impl;

import com.SmartHire.hrService.dto.JobPositionCreateDTO;
import com.SmartHire.hrService.dto.JobPositionListDTO;
import com.SmartHire.hrService.dto.JobPositionUpdateDTO;
import com.SmartHire.hrService.dto.JobSkillDTO;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobPositionMapper;
import com.SmartHire.hrService.mapper.JobSkillRequirementMapper;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobPosition;
import com.SmartHire.hrService.model.JobSkillRequirement;
import com.SmartHire.hrService.service.JobPositionService;
import com.SmartHire.shared.exception.enums.ErrorCode;
import com.SmartHire.shared.exception.exception.BusinessException;
import com.SmartHire.shared.utils.JwtUtil;
import com.SmartHire.shared.utils.SecurityContextUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类
 */
@Service
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {

    @Autowired
    private HrInfoMapper hrInfoMapper;

    @Autowired
    private JobSkillRequirementMapper jobSkillRequirementMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前登录HR的ID（hr_info表的id）
     * 注意：用户身份验证已由AOP切面统一处理
     */
    private Long getCurrentHrId() {
        Map<String, Object> map = SecurityContextUtil.getCurrentClaims();
        Long userId = jwtUtil.getUserIdFromToken(map);

        // 通过user_id查询hr_info表获取hr_id（hr_info.id）
        HrInfo hrInfo = hrInfoMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrInfo>()
                        .eq(HrInfo::getUserId, userId)
        );
        
        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        return hrInfo.getId(); // 返回hr_info表的id，这就是job_position表的hr_id
    }

    /**
     * 验证岗位是否属于当前HR
     */
    private void validateJobOwnership(Long jobId) {
        JobPosition jobPosition = getById(jobId);
        if (jobPosition == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        Long currentHrId = getCurrentHrId();
        if (!jobPosition.getHrId().equals(currentHrId)) {
            throw new BusinessException(ErrorCode.JOB_NOT_BELONG_TO_HR);
        }
    }

    @Override
    @Transactional
    public Long createJobPosition(JobPositionCreateDTO createDTO) {
        Long currentHrId = getCurrentHrId();

        // 创建岗位实体
        JobPosition jobPosition = new JobPosition();
        BeanUtils.copyProperties(createDTO, jobPosition);
        jobPosition.setHrId(currentHrId);
        
        // 设置默认值
        if (jobPosition.getStatus() == null) {
            jobPosition.setStatus(1); // 默认招聘中
        }
        if (jobPosition.getSalaryMonths() == null) {
            jobPosition.setSalaryMonths(12); // 默认12个月
        }
        if (jobPosition.getViewCount() == null) {
            jobPosition.setViewCount(0);
        }
        if (jobPosition.getApplicationCount() == null) {
            jobPosition.setApplicationCount(0);
        }

        Date now = new Date();
        jobPosition.setCreatedAt(now);
        jobPosition.setUpdatedAt(now);
        if (jobPosition.getStatus() == 1) {
            jobPosition.setPublishedAt(now); // 如果状态是招聘中，设置发布时间
        }

        // 保存岗位
        save(jobPosition);

        // 保存技能要求
        if (createDTO.getSkills() != null && !createDTO.getSkills().isEmpty()) {
            List<JobSkillRequirement> skillRequirements = createDTO.getSkills().stream()
                    .map(skill -> {
                        JobSkillRequirement requirement = new JobSkillRequirement();
                        requirement.setJobId(jobPosition.getId());
                        requirement.setSkillName(skill.getSkillName());
                        requirement.setIsRequired(skill.getIsRequired() != null ? skill.getIsRequired() : 1);
                        requirement.setCreatedAt(now);
                        return requirement;
                    })
                    .collect(Collectors.toList());
            
            for (JobSkillRequirement requirement : skillRequirements) {
                jobSkillRequirementMapper.insert(requirement);
            }
        }

        return jobPosition.getId();
    }

    @Override
    @Transactional
    public void updateJobPosition(Long jobId, JobPositionUpdateDTO updateDTO) {
        validateJobOwnership(jobId);

        JobPosition jobPosition = getById(jobId);
        
        // 更新字段（只更新非空字段）
        if (StringUtils.hasText(updateDTO.getJobTitle())) {
            jobPosition.setJobTitle(updateDTO.getJobTitle());
        }
        if (updateDTO.getJobCategory() != null) {
            jobPosition.setJobCategory(updateDTO.getJobCategory());
        }
        if (updateDTO.getDepartment() != null) {
            jobPosition.setDepartment(updateDTO.getDepartment());
        }
        if (StringUtils.hasText(updateDTO.getCity())) {
            jobPosition.setCity(updateDTO.getCity());
        }
        if (updateDTO.getAddress() != null) {
            jobPosition.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getSalaryMin() != null) {
            jobPosition.setSalaryMin(updateDTO.getSalaryMin());
        }
        if (updateDTO.getSalaryMax() != null) {
            jobPosition.setSalaryMax(updateDTO.getSalaryMax());
        }
        if (updateDTO.getSalaryMonths() != null) {
            jobPosition.setSalaryMonths(updateDTO.getSalaryMonths());
        }
        if (updateDTO.getEducationRequired() != null) {
            jobPosition.setEducationRequired(updateDTO.getEducationRequired());
        }
        if (updateDTO.getJobType() != null) {
            jobPosition.setJobType(updateDTO.getJobType());
        }
        if (StringUtils.hasText(updateDTO.getDescription())) {
            jobPosition.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getResponsibilities() != null) {
            jobPosition.setResponsibilities(updateDTO.getResponsibilities());
        }
        if (updateDTO.getRequirements() != null) {
            jobPosition.setRequirements(updateDTO.getRequirements());
        }
        if (updateDTO.getStatus() != null) {
            jobPosition.setStatus(updateDTO.getStatus());
            // 如果状态改为招聘中且之前没有发布时间，设置发布时间
            if (updateDTO.getStatus() == 1 && jobPosition.getPublishedAt() == null) {
                jobPosition.setPublishedAt(new Date());
            }
        }

        jobPosition.setUpdatedAt(new Date());
        updateById(jobPosition);
    }

    @Override
    public void offlineJobPosition(Long jobId) {
        validateJobOwnership(jobId);
        updateJobPositionStatus(jobId, 0);
    }

    @Override
    public List<JobPositionListDTO> getJobPositionList(Integer status) {
        Long currentHrId = getCurrentHrId();

        // 构建查询条件
        List<JobPosition> jobPositions;
        if (status != null) {
            jobPositions = lambdaQuery()
                    .eq(JobPosition::getHrId, currentHrId)
                    .eq(JobPosition::getStatus, status)
                    .orderByDesc(JobPosition::getCreatedAt)
                    .list();
        } else {
            jobPositions = lambdaQuery()
                    .eq(JobPosition::getHrId, currentHrId)
                    .orderByDesc(JobPosition::getCreatedAt)
                    .list();
        }

        // 转换为DTO并填充技能要求
        return jobPositions.stream()
                .map(job -> {
                    JobPositionListDTO dto = new JobPositionListDTO();
                    BeanUtils.copyProperties(job, dto);
                    
                    // 查询技能要求
                    List<String> skills = jobSkillRequirementMapper.selectSkillNamesByJobId(job.getId());
                    dto.setSkills(skills);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateJobPositionStatus(Long jobId, Integer status) {
        validateJobOwnership(jobId);

        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        JobPosition jobPosition = getById(jobId);
        jobPosition.setStatus(status);
        
        // 如果状态改为招聘中且之前没有发布时间，设置发布时间
        if (status == 1 && jobPosition.getPublishedAt() == null) {
            jobPosition.setPublishedAt(new Date());
        }
        
        jobPosition.setUpdatedAt(new Date());
        updateById(jobPosition);
    }

    @Override
    public JobPositionListDTO getJobPositionById(Long jobId) {
        validateJobOwnership(jobId);

        JobPosition jobPosition = getById(jobId);
        if (jobPosition == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        JobPositionListDTO dto = new JobPositionListDTO();
        BeanUtils.copyProperties(jobPosition, dto);

        // 查询技能要求
        List<String> skills = jobSkillRequirementMapper.selectSkillNamesByJobId(jobId);
        dto.setSkills(skills);

        return dto;
    }
}

