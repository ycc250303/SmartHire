package com.SmartHire.adminService.service.impl;

import com.SmartHire.adminService.dto.JobAuditListDTO;
import com.SmartHire.adminService.dto.JobAuditQueryDTO;
import com.SmartHire.adminService.enums.AuditStatus;
import com.SmartHire.common.exception.exception.AdminServiceException;
import com.SmartHire.adminService.mapper.JobAuditMapper;
import com.SmartHire.adminService.model.JobAuditRecord;
import com.SmartHire.adminService.service.JobAuditService;
import com.SmartHire.common.api.HrApi;
import com.SmartHire.common.api.UserAuthApi;
import com.SmartHire.hrService.model.Company;
import com.SmartHire.hrService.model.HrInfo;
import com.SmartHire.hrService.model.JobInfo;
import com.SmartHire.userAuthService.model.User;
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
  private final HrApi hrApi;
  private final UserAuthApi userAuthApi;

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
        wrapper.orderByDesc(JobAuditRecord::getId); // 按ID降序，获取最新记录
        wrapper.last("LIMIT 1"); // 限制只返回一条记录
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOfflineJob(Long jobId, String offlineReason, Long auditorId, String auditorName) {
        log.info("开始强制下线职位，职位ID: {}, 审核员: {}, 下线原因: {}", jobId, auditorName, offlineReason);

        // 验证职位存在且为已通过系统审核状态
        JobAuditRecord auditRecord = validateJobForOffline(jobId);

        Date now = new Date();

        // 更新审核记录：设置状态为rejected，使用reject_reason记录下线原因
        auditRecord.setSystemAuditStatus("rejected"); // 设置为rejected状态
        auditRecord.setSystemAuditorId(auditorId);
        auditRecord.setSystemAuditorName(auditorName);
        auditRecord.setSystemAuditedAt(now);
        // 更新兼容字段
        auditRecord.setStatus("rejected"); // 兼容字段
        auditRecord.setRejectReason("强制下线：" + offlineReason); // 记录下线原因
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditorName(auditorName);
        auditRecord.setAuditedAt(now);
        updateById(auditRecord);

        // 更新JobInfo表：设置职位为下线状态，审核状态为rejected
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(jobId);
        jobInfo.setStatus(0); // 0-已下线
        jobInfo.setAuditStatus("rejected"); // 设置审核状态为rejected
        jobInfo.setAuditedAt(now);
        jobInfo.setUpdatedAt(now);
        hrApi.updateJobInfo(jobInfo);

        log.info("职位强制下线完成，职位ID: {}", jobId);
    }

    /**
     * 验证职位是否可以强制下线
     * 系统管理员只能下线同时通过公司审核和系统审核的职位
     *
     * @param jobId 职位ID
     * @return 审核记录
     */
    private JobAuditRecord validateJobForOffline(Long jobId) {
        // 1. 验证职位及其关联数据的存在性
        validateJobExistence(jobId);

        JobAuditRecord auditRecord = getAuditDetail(jobId);
        if (auditRecord == null) {
            throw AdminServiceException.jobNotFound(jobId);
        }

        // 验证必须已通过公司审核
        if (!AuditStatus.APPROVED.getCode().equals(auditRecord.getCompanyAuditStatus())) {
            throw AdminServiceException.operationFailed("该岗位尚未通过公司审核，无法进行下线操作");
        }

        // 验证必须已通过系统管理员审核且未被强制下线
        String systemAuditStatus = auditRecord.getSystemAuditStatus();
        if (systemAuditStatus == null || !AuditStatus.APPROVED.getCode().equals(systemAuditStatus)) {
            throw AdminServiceException.operationFailed("只有已通过系统管理员审核的职位才能强制下线");
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
  @SuppressWarnings("unused")
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
    // 1. 验证职位是否存在以及关联的HR和公司是否存在
    validateJobExistence(jobId);

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
   * 验证职位及其关联的HR和公司的存在性
   *
   * @param jobId 职位ID
   */
  private void validateJobExistence(Long jobId) {
    // 验证职位是否存在
    JobInfo jobInfo = hrApi.getJobInfoById(jobId);
    if (jobInfo == null) {
      throw AdminServiceException.jobNotFound(jobId);
    }

    // 验证HR是否存在
    if (jobInfo.getHrId() != null) {
      HrInfo hrInfo = hrApi.getHrInfoById(jobInfo.getHrId());
      if (hrInfo == null) {
        throw AdminServiceException.operationFailed("职位关联的HR不存在，HR ID: " + jobInfo.getHrId());
      }

      // 验证HR关联的用户是否存在且用户类型正确
      if (hrInfo.getUserId() != null) {
        if (!userAuthApi.existsUser(hrInfo.getUserId())) {
          throw AdminServiceException.operationFailed("HR关联的用户不存在，用户 ID: " + hrInfo.getUserId());
        }
        // 验证用户类型是否为HR
        if (!userAuthApi.validateUserType(hrInfo.getUserId(), 2)) {
          throw AdminServiceException.operationFailed("HR关联的用户类型不正确，用户 ID: " + hrInfo.getUserId());
        }
      }
    }

    // 验证公司是否存在
    if (jobInfo.getCompanyId() != null) {
      Company company = hrApi.getCompanyById(jobInfo.getCompanyId());
      if (company == null) {
        throw AdminServiceException.operationFailed("职位关联的公司不存在，公司 ID: " + jobInfo.getCompanyId());
      }
    }
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
    hrApi.updateJobInfo(jobInfo);
  }
}
