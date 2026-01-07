package com.SmartHire.hrService.service.impl;

import com.SmartHire.common.auth.UserContext;
import com.SmartHire.common.exception.enums.ErrorCode;
import com.SmartHire.common.exception.exception.BusinessException;
import com.SmartHire.hrService.dto.CompanyDTO;
import com.SmartHire.hrService.dto.CompanyNameDTO;
import com.SmartHire.hrService.dto.HrInfoDTO;
import com.SmartHire.hrService.mapper.CompanyMapper;
import com.SmartHire.hrService.mapper.HrAuditMapper;
import com.SmartHire.hrService.mapper.HrInfoMapper;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrAuditRecord;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.hrService.service.CompanyService;
import com.SmartHire.adminService.enums.AuditStatus;
import com.SmartHire.adminService.mapper.JobAuditMapper;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 公司管理员服务实现类
 *
 * @author SmartHire Team
 * @since 2025-12-13
 */
@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private UserContext userContext;

    @Autowired
    private HrInfoMapper hrInfoMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private HrAuditMapper hrAuditMapper;

    @Autowired
    private JobInfoMapper jobInfoMapper;

    @Autowired
    private JobAuditMapper jobAuditMapper;

    @Override
    public Long createCompany(CompanyDTO createDTO) {
        Long userId = userContext.getCurrentUserId();

        Company company = new Company();
        company.setCompanyName(createDTO.getCompanyName());
        company.setOwnerUserId(userId);
        company.setDescription(createDTO.getDescription());
        company.setCompanyScale(createDTO.getCompanyScale());
        company.setFinancingStage(createDTO.getFinancingStage());
        company.setIndustry(createDTO.getIndustry());
        company.setWebsite(createDTO.getWebsite());
        company.setLogoUrl(createDTO.getLogoUrl());
        company.setBenefits(createDTO.getBenefits());
        company.setCompanyCreatedAt(createDTO.getCompanyCreatedAt());
        company.setStatus(1);

        company.setCreatedAt(new Date());
        company.setUpdatedAt(new Date());
        companyMapper.insert(company);
        // TODO：管理员审核

        return company.getId();
    }

    @Override
    public CompanyDTO getCompanyById(Long companyId) {
        Company company = companyMapper.selectById(companyId);
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setCompanyName(company.getCompanyName());
        companyDTO.setDescription(company.getDescription());
        companyDTO.setCompanyScale(company.getCompanyScale());
        companyDTO.setFinancingStage(company.getFinancingStage());
        companyDTO.setIndustry(company.getIndustry());
        companyDTO.setWebsite(company.getWebsite());
        companyDTO.setLogoUrl(company.getLogoUrl());
        companyDTO.setBenefits(company.getBenefits());
        companyDTO.setRegisteredCapital(company.getRegisteredCapital());
        return companyDTO;
    }

    @Override
    public List<CompanyNameDTO> getCompanies(Long current, Long size, String keyword) {
        Page<Company> page = new Page<>(current, size);
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Company::getCompanyName, keyword);
        }
        // TODO：只查询已通过审核且状态正常的公司
        // wrapper.eq(Company::getStatus, 1);

        Page<Company> result = companyMapper.selectPage(page, wrapper);

        return result.getRecords().stream()
                .map(company -> {
                    CompanyNameDTO dto = new CompanyNameDTO();
                    dto.setCompanyId(company.getId());
                    dto.setCompanyName(company.getCompanyName());
                    return dto;
                })
                .toList();
    }

    /**
     * 获取当前登录HR信息并验证是否为公司管理员
     *
     * @return HR信息
     */
    private HrInfo getCurrentHrInfoAndValidateAdmin() {
        Long userId = userContext.getCurrentUserId();
        HrInfo hrInfo = hrInfoMapper.selectOne(
                new LambdaQueryWrapper<HrInfo>().eq(HrInfo::getUserId, userId));

        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        if (hrInfo.getIsCompanyAdmin() != 1) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只有公司管理员才能执行此操作");
        }

        return hrInfo;
    }

    /**
     * 验证操作的是本公司的数据
     *
     * @param companyId 公司ID
     */
    private void validateCompanyOwnership(Long companyId) {
        Long currentUserId = userContext.getCurrentUserId();
        // 检查当前用户是否是该公司的 HR 成员
        HrInfo hrInfo = hrInfoMapper.selectOne(
                new LambdaQueryWrapper<HrInfo>()
                        .eq(HrInfo::getUserId, currentUserId)
                        .eq(HrInfo::getCompanyId, companyId));

        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.NOT_COMPANY_DATA);
        }

        if (hrInfo.getIsCompanyAdmin() != 1) {
            throw new BusinessException(ErrorCode.NOT_COMPANY_ADMIN);
        }
    }

    @Override
    @Transactional
    public void updateCompanyInfo(Long companyId, CompanyDTO companyDTO) {
        validateCompanyOwnership(companyId);

        Company existingCompany = companyMapper.selectById(companyId);
        if (existingCompany == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_EXIST);
        }

        // 更新公司信息
        Company company = new Company();
        company.setId(companyId);
        company.setCompanyName(companyDTO.getCompanyName());
        company.setDescription(companyDTO.getDescription());
        company.setCompanyScale(companyDTO.getCompanyScale());
        company.setFinancingStage(companyDTO.getFinancingStage());
        company.setIndustry(companyDTO.getIndustry());
        company.setWebsite(companyDTO.getWebsite());
        company.setLogoUrl(companyDTO.getLogoUrl());
        company.setBenefits(companyDTO.getBenefits());
        company.setRegisteredCapital(companyDTO.getRegisteredCapital());
        company.setUpdatedAt(new Date());
        companyMapper.updateById(company);
    }

    @Override
    public Page<HrInfoDTO> getCompanyHrList(Page<HrInfoDTO> page, String keyword) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        // 查询本公司所有HR
        LambdaQueryWrapper<HrInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrInfo::getCompanyId, companyId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(
                    w -> w.like(HrInfo::getRealName, keyword)
                            .or()
                            .like(HrInfo::getPosition, keyword)
                            .or()
                            .like(HrInfo::getWorkPhone, keyword));
        }

        Page<HrInfo> hrPage = new Page<>(page.getCurrent(), page.getSize());
        Page<HrInfo> result = hrInfoMapper.selectPage(hrPage, wrapper);

        // 转换为DTO
        Page<HrInfoDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(
                result.getRecords().stream()
                        .map(
                                hr -> {
                                    HrInfoDTO dto = new HrInfoDTO();
                                    dto.setId(hr.getId());
                                    dto.setUserId(hr.getUserId());
                                    dto.setCompanyId(hr.getCompanyId());
                                    dto.setRealName(hr.getRealName());
                                    dto.setPosition(hr.getPosition());
                                    dto.setWorkPhone(hr.getWorkPhone());
                                    dto.setCreatedAt(hr.getCreatedAt());
                                    dto.setUpdatedAt(hr.getUpdatedAt());
                                    // 查询公司名称
                                    Company company = companyMapper.selectById(hr.getCompanyId());
                                    if (company != null) {
                                        dto.setCompanyName(company.getCompanyName());
                                    }
                                    return dto;
                                })
                        .toList());

        return dtoPage;
    }

    @Override
    @Transactional
    public void approveHr(Long hrId) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        HrInfo hrInfo = hrInfoMapper.selectById(hrId);
        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        if (!hrInfo.getCompanyId().equals(companyId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能审核本公司的HR");
        }

        // 创建审核记录
        HrAuditRecord auditRecord = new HrAuditRecord();
        auditRecord.setHrId(hrId);
        auditRecord.setUserId(hrInfo.getUserId());
        auditRecord.setCompanyId(companyId);
        auditRecord.setHrName(hrInfo.getRealName());
        auditRecord.setAuditStatus(AuditStatus.APPROVED.getCode());
        auditRecord.setAuditorId(currentHr.getId());
        auditRecord.setAuditorName(currentHr.getRealName());
        auditRecord.setAuditedAt(new Date());
        hrAuditMapper.insert(auditRecord);
    }

    @Override
    @Transactional
    public void rejectHr(Long hrId, String rejectReason) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        HrInfo hrInfo = hrInfoMapper.selectById(hrId);
        if (hrInfo == null) {
            throw new BusinessException(ErrorCode.HR_NOT_EXIST);
        }

        if (!hrInfo.getCompanyId().equals(companyId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能审核本公司的HR");
        }

        // 创建审核记录
        HrAuditRecord auditRecord = new HrAuditRecord();
        auditRecord.setHrId(hrId);
        auditRecord.setUserId(hrInfo.getUserId());
        auditRecord.setCompanyId(companyId);
        auditRecord.setHrName(hrInfo.getRealName());
        auditRecord.setAuditStatus(AuditStatus.REJECTED.getCode());
        auditRecord.setAuditorId(currentHr.getId());
        auditRecord.setAuditorName(currentHr.getRealName());
        auditRecord.setRejectReason(rejectReason);
        auditRecord.setAuditedAt(new Date());
        hrAuditMapper.insert(auditRecord);
    }

    @Override
    public Page<com.SmartHire.adminService.dto.JobAuditListDTO> getCompanyJobAuditList(
            Page<com.SmartHire.adminService.dto.JobAuditListDTO> page, String status, String keyword) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        // 如果没有指定状态，默认查询待审核的
        if (status == null || status.trim().isEmpty()) {
            status = AuditStatus.PENDING.getCode();
        }

        // 使用 Mapper 查询完整的 DTO 列表
        return jobAuditMapper.selectCompanyAuditList(page, companyId, status, keyword);
    }

    @Override
    @Transactional
    public void approveCompanyJob(Long jobId) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        JobInfo jobInfo = jobInfoMapper.selectById(jobId);
        if (jobInfo == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobInfo.getCompanyId().equals(companyId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能审核本公司的岗位");
        }

        // 查询审核记录
        LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAuditRecord::getJobId, jobId);
        JobAuditRecord auditRecord = jobAuditMapper.selectOne(wrapper);

        if (auditRecord == null) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "审核记录不存在");
        }

        if (!AuditStatus.PENDING.getCode().equals(auditRecord.getCompanyAuditStatus())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "岗位状态不正确，无法进行审核操作");
        }

        Date now = new Date();

        // 更新审核记录
        auditRecord.setCompanyAuditStatus(AuditStatus.APPROVED.getCode());
        auditRecord.setCompanyAuditorId(currentHr.getId());
        auditRecord.setCompanyAuditorName(currentHr.getRealName());
        auditRecord.setCompanyAuditedAt(now);
        // 设置为待系统审核
        auditRecord.setSystemAuditStatus(AuditStatus.PENDING.getCode());
        auditRecord.setStatus("system_pending"); // 兼容字段
        jobAuditMapper.updateById(auditRecord);

        // 更新岗位状态
        jobInfo.setCompanyAuditStatus(AuditStatus.APPROVED.getCode());
        jobInfo.setAuditStatus("system_pending");
        jobInfo.setAuditedAt(now);
        jobInfoMapper.updateById(jobInfo);
    }

    @Override
    @Transactional
    public void rejectCompanyJob(Long jobId, String rejectReason) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        JobInfo jobInfo = jobInfoMapper.selectById(jobId);
        if (jobInfo == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobInfo.getCompanyId().equals(companyId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能审核本公司的岗位");
        }

        // 查询审核记录
        LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAuditRecord::getJobId, jobId);
        JobAuditRecord auditRecord = jobAuditMapper.selectOne(wrapper);

        if (auditRecord == null) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "审核记录不存在");
        }

        if (!AuditStatus.PENDING.getCode().equals(auditRecord.getCompanyAuditStatus())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "岗位状态不正确，无法进行审核操作");
        }

        Date now = new Date();

        // 更新审核记录
        auditRecord.setCompanyAuditStatus(AuditStatus.REJECTED.getCode());
        auditRecord.setCompanyAuditorId(currentHr.getId());
        auditRecord.setCompanyAuditorName(currentHr.getRealName());
        auditRecord.setRejectReason(rejectReason);
        auditRecord.setCompanyAuditedAt(now);
        auditRecord.setStatus(AuditStatus.REJECTED.getCode()); // 兼容字段
        jobAuditMapper.updateById(auditRecord);

        // 更新岗位状态
        jobInfo.setCompanyAuditStatus(AuditStatus.REJECTED.getCode());
        jobInfo.setAuditStatus(AuditStatus.REJECTED.getCode());
        jobInfo.setAuditedAt(now);
        jobInfoMapper.updateById(jobInfo);
    }

    @Override
    @Transactional
    public void modifyCompanyJob(Long jobId, String modifyReason) {
        HrInfo currentHr = getCurrentHrInfoAndValidateAdmin();
        Long companyId = currentHr.getCompanyId();

        JobInfo jobInfo = jobInfoMapper.selectById(jobId);
        if (jobInfo == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_EXIST);
        }

        if (!jobInfo.getCompanyId().equals(companyId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "只能审核本公司的岗位");
        }

        // 查询审核记录
        LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAuditRecord::getJobId, jobId);
        JobAuditRecord auditRecord = jobAuditMapper.selectOne(wrapper);

        if (auditRecord == null) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "审核记录不存在");
        }

        if (!AuditStatus.PENDING.getCode().equals(auditRecord.getCompanyAuditStatus())) {
            throw new BusinessException(ErrorCode.ADMIN_OPERATION_FAILED, "岗位状态不正确，无法进行审核操作");
        }

        Date now = new Date();

        // 更新审核记录
        auditRecord.setCompanyAuditStatus(AuditStatus.MODIFIED.getCode());
        auditRecord.setCompanyAuditorId(currentHr.getId());
        auditRecord.setCompanyAuditorName(currentHr.getRealName());
        auditRecord.setAuditReason(modifyReason);
        auditRecord.setCompanyAuditedAt(now);
        auditRecord.setStatus(AuditStatus.MODIFIED.getCode()); // 兼容字段
        jobAuditMapper.updateById(auditRecord);

        // 更新岗位状态
        jobInfo.setCompanyAuditStatus(AuditStatus.MODIFIED.getCode());
        jobInfo.setAuditStatus(AuditStatus.MODIFIED.getCode());
        jobInfo.setAuditedAt(now);
        jobInfoMapper.updateById(jobInfo);
    }
}
