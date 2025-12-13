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
  public Page<JobAuditListDTO> getAuditList(Page<JobAuditListDTO> page, JobAuditQueryDTO queryDTO) {
    return jobAuditMapper.selectAuditList(page, queryDTO.getStatus(), queryDTO.getKeyword());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void approveJob(Long jobId, Long auditorId, String auditorName) {
    log.info("开始审核通过职位，职位ID: {}, 审核员: {}", jobId, auditorName);

    // 验证职位状态
    JobAuditRecord auditRecord = validateJobStatus(jobId, AuditStatus.PENDING);

    // 更新审核记录
    auditRecord.setStatus(AuditStatus.APPROVED.getCode());
    auditRecord.setAuditorId(auditorId);
    auditRecord.setAuditorName(auditorName);
    auditRecord.setAuditedAt(new Date());
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.APPROVED);

    log.info("职位审核通过完成，职位ID: {}", jobId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void rejectJob(Long jobId, String rejectReason, Long auditorId, String auditorName) {
    log.info("开始拒绝职位，职位ID: {}, 审核员: {}, 拒绝原因: {}", jobId, auditorName, rejectReason);

    // 验证职位状态
    JobAuditRecord auditRecord = validateJobStatus(jobId, AuditStatus.PENDING);

    // 更新审核记录
    auditRecord.setStatus(AuditStatus.REJECTED.getCode());
    auditRecord.setRejectReason(rejectReason);
    auditRecord.setAuditorId(auditorId);
    auditRecord.setAuditorName(auditorName);
    auditRecord.setAuditedAt(new Date());
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.REJECTED);

    log.info("职位拒绝审核完成，职位ID: {}", jobId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void modifyJob(Long jobId, String modifyReason, Long auditorId, String auditorName) {
    log.info("开始要求修改职位，职位ID: {}, 审核员: {}, 修改意见: {}", jobId, auditorName, modifyReason);

    // 验证职位状态
    JobAuditRecord auditRecord = validateJobStatus(jobId, AuditStatus.PENDING);

    // 更新审核记录
    auditRecord.setStatus(AuditStatus.MODIFIED.getCode());
    auditRecord.setAuditReason(modifyReason);
    auditRecord.setAuditorId(auditorId);
    auditRecord.setAuditorName(auditorName);
    auditRecord.setAuditedAt(new Date());
    updateById(auditRecord);

    // 更新职位状态
    updateJobInfoAuditStatus(jobId, AuditStatus.MODIFIED);

    log.info("职位要求修改完成，职位ID: {}", jobId);
  }

  @Override
  public Integer countByStatus(String status) {
    return jobAuditMapper.countByStatus(status);
  }

  @Override
  public JobAuditRecord getAuditDetail(Long jobId) {
    LambdaQueryWrapper<JobAuditRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(JobAuditRecord::getJobId, jobId);
    return getOne(wrapper);
  }

  /**
   * 验证职位状态
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
