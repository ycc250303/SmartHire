package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.SmartHire.adminService.dto.JobAuditQueryDTO;
import com.SmartHire.adminService.enums.AuditStatus;
import com.SmartHire.adminService.exception.AdminServiceException;
import com.SmartHire.adminService.mapper.JobAuditMapper;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.SmartHire.adminService.service.JobAuditService;
import com.SmartHire.hrService.mapper.JobInfoMapper;
import com.SmartHire.hrService.model.JobInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 职位审核Service实现类
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobAuditServiceImpl extends ServiceImpl<JobAuditMapper, JobAuditRecord>
    implements JobAuditService {

  private final JobAuditMapper jobAuditMapper;
  private final JobInfoMapper jobInfoMapper;

  @Override
  public Page<JobAuditListDTO> getAuditList(Page<JobAuditListDTO> page,
                                            JobAuditQueryDTO queryDTO) {
    // 系统管理员只查看已通过公司审核的岗位（company_audit_status = 'approved'）
    // 查询时使用 system_audit_status 作为状态筛选
    return jobAuditMapper.selectSystemAuditList(page, queryDTO.getStatus(), queryDTO.getKeyword());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void approveJob(Long jobId, Long auditorId, String auditorName) {
    log.info("开始系统审核通过职位，职位ID: {}, 审核员: {}", jobId, auditorName);

    // 验证职位状态：必须已通过公司审核，且系统审核状态为待审核
    JobAuditRecord auditRecord = validateSystemJobStatus(jobId, AuditStatus.PENDING);

    Date now = new Date();

    // 更新审核记录（系统审核）
    auditRecord.setSystemAuditStatus(AuditStatus.APPROVED.getCode());
    auditRecord.setSystemAuditorId(auditorId);
    auditRecord.setSystemAuditorName(auditorName);
    auditRecord.setSystemAuditedAt(now);
    auditRecord.setStatus(AuditStatus.APPROVED.getCode()); // 兼容字段
    auditRecord.setAuditorId(auditorId); // 兼容字段
    auditRecord.setAuditorName(auditorName); // 兼容字段
    auditRecord.setAuditedAt(now); // 兼容字段
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.APPROVED);

    log.info("职位系统审核通过完成，职位ID: {}", jobId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void rejectJob(Long jobId, String rejectReason, Long auditorId, String auditorName) {
    log.info("开始系统审核拒绝职位，职位ID: {}, 审核员: {}, 拒绝原因: {}", jobId, auditorName, rejectReason);

    // 验证职位状态：必须已通过公司审核，且系统审核状态为待审核
    JobAuditRecord auditRecord = validateSystemJobStatus(jobId, AuditStatus.PENDING);

    Date now = new Date();

    // 更新审核记录（系统审核）
    auditRecord.setSystemAuditStatus(AuditStatus.REJECTED.getCode());
    auditRecord.setSystemAuditorId(auditorId);
    auditRecord.setSystemAuditorName(auditorName);
    auditRecord.setRejectReason(rejectReason);
    auditRecord.setSystemAuditedAt(now);
    auditRecord.setStatus(AuditStatus.REJECTED.getCode()); // 兼容字段
    auditRecord.setAuditorId(auditorId); // 兼容字段
    auditRecord.setAuditorName(auditorName); // 兼容字段
    auditRecord.setAuditedAt(now); // 兼容字段
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.REJECTED);

    log.info("职位系统审核拒绝完成，职位ID: {}", jobId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void modifyJob(Long jobId, String modifyReason, Long auditorId, String auditorName) {
    log.info("开始系统审核要求修改职位，职位ID: {}, 审核员: {}, 修改意见: {}", jobId, auditorName, modifyReason);

    // 验证职位状态：必须已通过公司审核，且系统审核状态为待审核
    JobAuditRecord auditRecord = validateSystemJobStatus(jobId, AuditStatus.PENDING);

    Date now = new Date();

    // 更新审核记录（系统审核）
    auditRecord.setSystemAuditStatus(AuditStatus.MODIFIED.getCode());
    auditRecord.setSystemAuditorId(auditorId);
    auditRecord.setSystemAuditorName(auditorName);
    auditRecord.setAuditReason(modifyReason);
    auditRecord.setSystemAuditedAt(now);
    auditRecord.setStatus(AuditStatus.MODIFIED.getCode()); // 兼容字段
    auditRecord.setAuditorId(auditorId); // 兼容字段
    auditRecord.setAuditorName(auditorName); // 兼容字段
    auditRecord.setAuditedAt(now); // 兼容字段
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.MODIFIED);

    log.info("职位系统审核要求修改完成，职位ID: {}", jobId);
  }

  @Override
  public Integer countByStatus(String status) {
    // 系统管理员统计的是 system_audit_status，且只统计已通过公司审核的
    return jobAuditMapper.countBySystemStatus(status);
  }

    @Override
    public JobAuditRecord getAuditDetail(Long jobId) {
        LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobAuditRecord::getJobId, jobId);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOfflineJob(Long jobId, String offlineReason, Long auditorId, String auditorName) {
        log.info("开始强制下线职位，职位ID: {}, 审核员: {}, 下线原因: {}", jobId, auditorName, offlineReason);

        // 验证职位存在且为已通过状态
        JobAuditRecord auditRecord = validateJobForOffline(jobId);

        // 更新审核记录：记录强制下线操作
        auditRecord.setStatus(AuditStatus.REJECTED.getCode()); // 使用rejected状态表示被拒绝/下线
        auditRecord.setRejectReason(offlineReason); // 使用reject_reason字段记录下线原因
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditorName(auditorName);
        auditRecord.setAuditedAt(new Date());
        updateById(auditRecord);

        // 更新JobInfo表：只更新status为已下线，audit_status保持为rejected
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(jobId);
        jobInfo.setStatus(0); // 0-已下线
        jobInfo.setAuditStatus(AuditStatus.REJECTED.getCode()); // 与审核记录保持一致
        jobInfo.setAuditedAt(new Date());
        jobInfo.setUpdatedAt(new Date());
        jobInfoMapper.updateById(jobInfo);

        log.info("职位强制下线完成，职位ID: {}", jobId);
    }

    /**
     * 验证职位是否可以强制下线
     *
     * @param jobId 职位ID
     * @return 审核记录
     */
    private JobAuditRecord validateJobForOffline(Long jobId) {
        JobAuditRecord auditRecord = getAuditDetail(jobId);
        if (auditRecord == null) {
            throw AdminServiceException.jobNotFound(jobId);
        }

        // 只有已通过的职位才能强制下线
        if (!AuditStatus.APPROVED.getCode().equals(auditRecord.getStatus())) {
            throw AdminServiceException.operationFailed("只有已通过审核的职位才能强制下线");
        }

        return auditRecord;
    }

  /**
   * 验证职位状态（兼容旧方法）
   *
   * @param jobId 职位ID
   * @param expectedStatus 期望状态
   * @return 审核记录
   */
  private JobAuditRecord validateJobStatus(Long jobId, AuditStatus expectedStatus) {
    JobAuditRecord auditRecord = getAuditDetail(jobId);
    if (auditRecord == null) {
      throw AdminServiceException.jobNotFound(jobId);
    }

    if (!expectedStatus.getCode().equals(auditRecord.getStatus())) {
      throw AdminServiceException.operationFailed("职位状态不正确，无法进行审核操作");
    }

    return auditRecord;
  }

  /**
   * 验证系统审核职位状态（必须已通过公司审核，且系统审核状态为待审核）
   *
   * @param jobId 职位ID
   * @param expectedSystemStatus 期望的系统审核状态
   * @return 审核记录
   */
  private JobAuditRecord validateSystemJobStatus(Long jobId, AuditStatus expectedSystemStatus) {
    JobAuditRecord auditRecord = getAuditDetail(jobId);
    if (auditRecord == null) {
      throw AdminServiceException.jobNotFound(jobId);
    }

    // 验证必须已通过公司审核
    if (!AuditStatus.APPROVED.getCode().equals(auditRecord.getCompanyAuditStatus())) {
      throw AdminServiceException.operationFailed("该岗位尚未通过公司审核，无法进行系统审核");
    }

    // 验证系统审核状态
    if (!expectedSystemStatus.getCode().equals(auditRecord.getSystemAuditStatus())) {
      throw AdminServiceException.operationFailed("职位系统审核状态不正确，无法进行审核操作");
    }

    return auditRecord;
  }

  /**
   * 更新职位审核状态
   *
   * @param jobId 职位ID
   * @param auditStatus 审核状态
   */
  private void updateJobInfoAuditStatus(Long jobId, AuditStatus auditStatus) {
    JobInfo jobInfo = new JobInfo();
    jobInfo.setId(jobId);
    jobInfo.setAuditStatus(auditStatus.getCode());
    jobInfo.setAuditedAt(new Date());
    jobInfoMapper.updateById(jobInfo);
  }
}
