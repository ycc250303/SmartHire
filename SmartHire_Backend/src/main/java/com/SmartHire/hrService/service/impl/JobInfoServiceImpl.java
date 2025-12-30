package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.JobInfoCreateDTO;
import com.SmartHire.hrService.dto.JobInfoListDTO;
import com.SmartHire.hrService.dto.JobInfoUpdateDTO;
import com.SmartHire.hrService.dto.JobCardDTO;
import com.SmartHire.hrService.dto.JobSearchDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.mapper.JobSkillRequirementMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.model.JobSkillRequirement;
import com.SmartHire.hrService.service.JobInfoService;
import com.SmartHire.adminService.mapper.JobAuditMapper;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.SmartHire.adminService.enums.AuditStatus;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 岗位服务实现类 */
@Service
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo>
    implements JobInfoService {

  @Autowired
  private HrInfoMapper hrInfoMapper;

  @Autowired
  private CompanyMapper companyMapper;

  @Autowired
  private JobSkillRequirementMapper jobSkillRequirementMapper;

  @Autowired
  private UserContext userContext;

  @Autowired
  private JobAuditMapper jobAuditMapper;

  /** 验证岗位是否属于当前HR */
  private void validateJobOwnership(Long jobId) {
    JobInfo jobInfo = getById(jobId);
    if (jobInfo == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    Long currentHrId = userContext.getCurrentHrId();
    if (!jobInfo.getHrId().equals(currentHrId)) {
      throw new BusinessException(ErrorCode.JOB_NOT_BELONG_TO_HR);
    }
  }

  @Override
  @Transactional
  public Long createJobInfo(JobInfoCreateDTO createDTO) {
    Long currentHrId = userContext.getCurrentHrId();

    // 验证全职类型字段
    if (createDTO.getJobType() != null
        && createDTO.getJobType() == 0
        && createDTO.getExperienceRequired() == null) {
      throw new BusinessException(ErrorCode.EXPERIENCE_REQUIRED_FOR_FULL_TIME);
    }

    // 验证实习类型字段
    if (createDTO.getJobType() != null && createDTO.getJobType() == 1) {
      if (createDTO.getInternshipDaysPerWeek() == null) {
        throw new BusinessException(ErrorCode.INTERNSHIP_DAYS_PER_WEEK_REQUIRED);
      }
      if (createDTO.getInternshipDurationMonths() == null) {
        throw new BusinessException(ErrorCode.INTERNSHIP_DURATION_MONTHS_REQUIRED);
      }
    }

    // 验证公司是否存在（外键验证）
    Company company = companyMapper.selectById(createDTO.getCompanyId());
    if (company == null) {
      throw new BusinessException(ErrorCode.COMPANY_NOT_EXIST);
    }

    // 创建岗位实体
    JobInfo jobInfo = new JobInfo();
    BeanUtils.copyProperties(createDTO, jobInfo);
    jobInfo.setHrId(currentHrId);

    // 设置默认值
    if (jobInfo.getStatus() == null) {
      jobInfo.setStatus(1); // 默认招聘中
    }
    if (jobInfo.getSalaryMonths() == null) {
      jobInfo.setSalaryMonths(12); // 默认12个月
    }
    if (jobInfo.getViewCount() == null) {
      jobInfo.setViewCount(0);
    }
    if (jobInfo.getApplicationCount() == null) {
      jobInfo.setApplicationCount(0);
    }

    Date now = new Date();
    jobInfo.setCreatedAt(now);
    jobInfo.setUpdatedAt(now);
    if (jobInfo.getStatus() == 1) {
      jobInfo.setPublishedAt(now); // 如果状态是招聘中，设置发布时间
    }

    // 保存岗位
    save(jobInfo);

    // 保存技能要求
    if (createDTO.getSkills() != null && !createDTO.getSkills().isEmpty()) {
      List<JobSkillRequirement> skillRequirements = createDTO.getSkills().stream()
          .map(
              skill -> {
                JobSkillRequirement requirement = new JobSkillRequirement();
                requirement.setJobId(jobInfo.getId());
                requirement.setSkillName(skill.getSkillName());
                requirement.setIsRequired(
                    skill.getIsRequired() != null ? skill.getIsRequired() : 1);
                requirement.setCreatedAt(now);
                return requirement;
              })
          .collect(Collectors.toList());

      for (JobSkillRequirement requirement : skillRequirements) {
        jobSkillRequirementMapper.insert(requirement);
      }
    }

    return jobInfo.getId();
  }

  @Override
  @Transactional
  public void updateJobInfo(Long jobId, JobInfoUpdateDTO updateDTO) {
    validateJobOwnership(jobId);

    JobInfo jobInfo = getById(jobId);

    // 验证全职类型字段
    Integer newJobType = updateDTO.getJobType() != null ? updateDTO.getJobType() : jobInfo.getJobType();
    if (newJobType != null && newJobType == 0) {
      // 如果更新为全职类型或当前就是全职类型，需要验证经验要求字段
      Integer experienceRequired = updateDTO.getExperienceRequired() != null
          ? updateDTO.getExperienceRequired()
          : jobInfo.getExperienceRequired();

      if (experienceRequired == null) {
        throw new BusinessException(ErrorCode.EXPERIENCE_REQUIRED_FOR_FULL_TIME);
      }
    }

    // 验证实习类型字段
    if (newJobType != null && newJobType == 1) {
      // 如果更新为实习类型或当前就是实习类型，需要验证实习字段
      Integer internshipDaysPerWeek = updateDTO.getInternshipDaysPerWeek() != null
          ? updateDTO.getInternshipDaysPerWeek()
          : jobInfo.getInternshipDaysPerWeek();
      Integer internshipDurationMonths = updateDTO.getInternshipDurationMonths() != null
          ? updateDTO.getInternshipDurationMonths()
          : jobInfo.getInternshipDurationMonths();

      if (internshipDaysPerWeek == null) {
        throw new BusinessException(ErrorCode.INTERNSHIP_DAYS_PER_WEEK_REQUIRED);
      }
      if (internshipDurationMonths == null) {
        throw new BusinessException(ErrorCode.INTERNSHIP_DURATION_MONTHS_REQUIRED);
      }
    }

    // 更新字段（只更新非空字段）
    if (StringUtils.hasText(updateDTO.getJobTitle())) {
      jobInfo.setJobTitle(updateDTO.getJobTitle());
    }
    if (updateDTO.getJobCategory() != null) {
      jobInfo.setJobCategory(updateDTO.getJobCategory());
    }
    if (updateDTO.getDepartment() != null) {
      jobInfo.setDepartment(updateDTO.getDepartment());
    }
    if (StringUtils.hasText(updateDTO.getCity())) {
      jobInfo.setCity(updateDTO.getCity());
    }
    if (updateDTO.getAddress() != null) {
      jobInfo.setAddress(updateDTO.getAddress());
    }
    if (updateDTO.getSalaryMin() != null) {
      jobInfo.setSalaryMin(updateDTO.getSalaryMin());
    }
    if (updateDTO.getSalaryMax() != null) {
      jobInfo.setSalaryMax(updateDTO.getSalaryMax());
    }
    if (updateDTO.getSalaryMonths() != null) {
      jobInfo.setSalaryMonths(updateDTO.getSalaryMonths());
    }
    if (updateDTO.getEducationRequired() != null) {
      jobInfo.setEducationRequired(updateDTO.getEducationRequired());
    }
    if (updateDTO.getJobType() != null) {
      jobInfo.setJobType(updateDTO.getJobType());
    }
    if (updateDTO.getExperienceRequired() != null) {
      jobInfo.setExperienceRequired(updateDTO.getExperienceRequired());
    }
    if (updateDTO.getInternshipDaysPerWeek() != null) {
      jobInfo.setInternshipDaysPerWeek(updateDTO.getInternshipDaysPerWeek());
    }
    if (updateDTO.getInternshipDurationMonths() != null) {
      jobInfo.setInternshipDurationMonths(updateDTO.getInternshipDurationMonths());
    }
    if (StringUtils.hasText(updateDTO.getDescription())) {
      jobInfo.setDescription(updateDTO.getDescription());
    }
    if (updateDTO.getResponsibilities() != null) {
      jobInfo.setResponsibilities(updateDTO.getResponsibilities());
    }
    if (updateDTO.getRequirements() != null) {
      jobInfo.setRequirements(updateDTO.getRequirements());
    }
    if (updateDTO.getStatus() != null) {
      jobInfo.setStatus(updateDTO.getStatus());
      // 如果状态改为招聘中且之前没有发布时间，设置发布时间
      if (updateDTO.getStatus() == 1 && jobInfo.getPublishedAt() == null) {
        jobInfo.setPublishedAt(new Date());
      }
    }

    jobInfo.setUpdatedAt(new Date());
    updateById(jobInfo);
  }

  @Override
  public void offlineJobInfo(Long jobId) {
    validateJobOwnership(jobId);
    updateJobInfoStatus(jobId, 0);
  }

  @Override
  public List<JobInfoListDTO> getJobInfoList(Integer status) {
    Long currentHrId = userContext.getCurrentHrId();

    // 构建查询条件
    List<JobInfo> jobInfos;
    if (status != null) {
      jobInfos = lambdaQuery()
          .eq(JobInfo::getHrId, currentHrId)
          .eq(JobInfo::getStatus, status)
          .orderByDesc(JobInfo::getCreatedAt)
          .list();
    } else {
      jobInfos = lambdaQuery().eq(JobInfo::getHrId, currentHrId).orderByDesc(JobInfo::getCreatedAt).list();
    }

    // 转换为DTO并填充技能要求
    return jobInfos.stream()
        .map(
            job -> {
              JobInfoListDTO dto = new JobInfoListDTO();
              BeanUtils.copyProperties(job, dto);

              // 查询技能要求
              List<String> skills = jobSkillRequirementMapper.selectSkillNamesByJobId(job.getId());
              dto.setSkills(skills);

              return dto;
            })
        .collect(Collectors.toList());
  }

  @Override
  public void updateJobInfoStatus(Long jobId, Integer status) {
    validateJobOwnership(jobId);

    if (status == null || (status != 0 && status != 1 && status != 2)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR);
    }

    JobInfo jobInfo = getById(jobId);
    jobInfo.setStatus(status);

    // 如果状态改为招聘中且之前没有发布时间，设置发布时间
    if (status == 1 && jobInfo.getPublishedAt() == null) {
      jobInfo.setPublishedAt(new Date());
    }

    jobInfo.setUpdatedAt(new Date());
    updateById(jobInfo);
  }

  @Override
  public JobInfoListDTO getJobInfoById(Long jobId) {
    // validateJobOwnership(jobId);

    JobInfo jobInfo = getById(jobId);
    if (jobInfo == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    JobInfoListDTO dto = new JobInfoListDTO();
    BeanUtils.copyProperties(jobInfo, dto);

    // 查询技能要求
    List<String> skills = jobSkillRequirementMapper.selectSkillNamesByJobId(jobId);
    dto.setSkills(skills);

    return dto;
  }

  @Override
  public List<JobCardDTO> searchPublicJobs(JobSearchDTO searchDTO) {
    int page = searchDTO.getPage() == null ? 1 : searchDTO.getPage();
    int size = searchDTO.getSize() == null ? 20 : searchDTO.getSize();
    int offset = (page - 1) * size;

    return baseMapper.searchPublicJobCards(
        searchDTO.getCity(),
        searchDTO.getJobType(),
        searchDTO.getEducationRequired(),
        searchDTO.getMinSalary(),
        searchDTO.getMaxSalary(),
        searchDTO.getKeyword(),
        searchDTO.getSkills(),
        searchDTO.getCompanyId(),
        offset,
        size);
  }

  @Override
  @Transactional
  public void submitJobForAudit(Long jobId) {
    // 验证岗位归属
    validateJobOwnership(jobId);

    JobInfo jobInfo = getById(jobId);
    if (jobInfo == null) {
      throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
    }

    // 检查岗位状态，允许草稿、需修改、已拒绝状态重新提交审核
    String currentStatus = jobInfo.getAuditStatus();
    if (currentStatus != null
        && !currentStatus.equals(AuditStatus.DRAFT.getCode())
        && !currentStatus.equals(AuditStatus.MODIFIED.getCode())
        && !currentStatus.equals(AuditStatus.REJECTED.getCode())) {
      throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "只有草稿、需修改或已拒绝状态的岗位才能提交审核");
    }

    // 获取HR信息
    Long currentHrId = userContext.getCurrentHrId();
    HrInfo hrInfo = hrInfoMapper.selectById(currentHrId);
    if (hrInfo == null) {
      throw new BusinessException(ErrorCode.HR_NOT_EXIST);
    }

    // 获取公司信息
    Company company = companyMapper.selectById(jobInfo.getCompanyId());
    if (company == null) {
      throw new BusinessException(ErrorCode.COMPANY_NOT_EXIST);
    }

    Date now = new Date();

    // 更新岗位状态为待公司审核
    jobInfo.setAuditStatus("company_pending");
    jobInfo.setCompanyAuditStatus(AuditStatus.PENDING.getCode());
    jobInfo.setSubmittedAt(now);
    updateById(jobInfo);

    // 创建或更新审核记录
    LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(JobAuditRecord::getJobId, jobId);

    JobAuditRecord auditRecord = jobAuditMapper.selectOne(wrapper);

    if (auditRecord == null) {
      // 创建新的审核记录
      auditRecord = new JobAuditRecord();
      auditRecord.setJobId(jobId);
      auditRecord.setJobTitle(jobInfo.getJobTitle());
      auditRecord.setCompanyId(jobInfo.getCompanyId());
      auditRecord.setHrId(currentHrId);
      auditRecord.setCompanyName(company.getCompanyName());
      auditRecord.setHrName(hrInfo.getRealName());
      auditRecord.setCompanyAuditStatus(AuditStatus.PENDING.getCode());
      auditRecord.setStatus("company_pending"); // 兼容字段
      jobAuditMapper.insert(auditRecord);
    } else {
      // 更新现有审核记录（重新提交审核）
      auditRecord.setCompanyAuditStatus(AuditStatus.PENDING.getCode());
      auditRecord.setStatus("company_pending"); // 兼容字段
      // 清空之前的审核信息（包括公司审核和系统审核字段）
      auditRecord.setCompanyAuditorId(null);
      auditRecord.setCompanyAuditorName(null);
      auditRecord.setCompanyAuditedAt(null);
      auditRecord.setSystemAuditStatus(null);
      auditRecord.setSystemAuditorId(null);
      auditRecord.setSystemAuditorName(null);
      auditRecord.setSystemAuditedAt(null);
      auditRecord.setRejectReason(null);
      auditRecord.setAuditReason(null);
      jobAuditMapper.updateById(auditRecord);
    }
  }
}
