package com.SmartHire.adminService.model;

import com.SmartHire.adminService.enums.AuditStatus;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 职位审核记录实体类
 *
 * @author SmartHire Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("job_audit_record")
public class JobAuditRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 审核记录ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 职位ID */
  @TableField("job_id")
  private Long jobId;

  /** 职位名称 */
  @TableField("job_title")
  private String jobTitle;

  /** 公司ID */
  @TableField("company_id")
  private Long companyId;

  /** HR ID */
  @TableField("hr_id")
  private Long hrId;

  /** 公司名称 */
  @TableField("company_name")
  private String companyName;

  /** HR姓名 */
  @TableField("hr_name")
  private String hrName;

  /** 审核备注 */
  @TableField("audit_note")
  private String auditNote;

  /** 审核意见（要求修改时使用） */
  @TableField("audit_reason")
  private String auditReason;

  /** 拒绝原因 */
  @TableField("reject_reason")
  private String rejectReason;

  /** 审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改 */
  @TableField("audit_status")
  private String status;

  /** 审核员ID */
  @TableField("auditor_id")
  private Long auditorId;

  /** 审核员姓名 */
  @TableField("auditor_name")
  private String auditorName;

  /**
   * 审核时间（保留用于兼容）
   */
  @TableField("audited_at")
  private Date auditedAt;

  // ========== 公司审核相关字段 ==========
  
  /**
   * 公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改
   */
  @TableField("company_audit_status")
  private String companyAuditStatus;

  /**
   * 公司审核员ID
   */
  @TableField("company_auditor_id")
  private Long companyAuditorId;

  /**
   * 公司审核员姓名
   */
  @TableField("company_auditor_name")
  private String companyAuditorName;

  /**
   * 公司审核时间
   */
  @TableField("company_audited_at")
  private Date companyAuditedAt;

  // ========== 系统审核相关字段 ==========
  
  /**
   * 系统审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改
   */
  @TableField("system_audit_status")
  private String systemAuditStatus;

  /**
   * 系统审核员ID
   */
  @TableField("system_auditor_id")
  private Long systemAuditorId;

  /**
   * 系统审核员姓名
   */
  @TableField("system_auditor_name")
  private String systemAuditorName;

  /**
   * 系统审核时间
   */
  @TableField("system_audited_at")
  private Date systemAuditedAt;

  /**
   * 创建时间
   */
  @TableField(value = "created_at", fill = FieldFill.INSERT)
  private Date createdAt;

  /** 更新时间 */
  @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
  private Date updatedAt;

  /**
   * 判断是否可以审核（公司审核）
   *
   * @return true如果可以审核
   */
  public boolean isCompanyAuditable() {
    return AuditStatus.PENDING.getCode().equals(this.companyAuditStatus);
  }

  /**
   * 判断是否可以审核（系统审核）
   *
   * @return true如果可以审核
   */
  public boolean isSystemAuditable() {
    return AuditStatus.PENDING.getCode().equals(this.systemAuditStatus) 
        && AuditStatus.APPROVED.getCode().equals(this.companyAuditStatus);
  }

  /**
   * 判断是否可以审核（兼容旧方法）
   *
   * @return true如果可以审核
   */
  public boolean isAuditable() {
    return AuditStatus.PENDING.getCode().equals(this.status);
  }

  /**
   * 获取状态描述
   *
   * @return 状态描述
   */
  public String getStatusDescription() {
    return AuditStatus.fromCode(this.status).getDescription();
  }

  // 防御性复制方法
  public Date getAuditedAt() {
    return auditedAt == null ? null : new Date(auditedAt.getTime());
  }

  public void setAuditedAt(Date auditedAt) {
    this.auditedAt = auditedAt == null ? null : new Date(auditedAt.getTime());
  }

  public Date getCreatedAt() {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
  }

  public Date getUpdatedAt() {
    return updatedAt == null ? null : new Date(updatedAt.getTime());
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt == null ? null : new Date(updatedAt.getTime());
  }

  // 防御性复制方法 - 公司审核时间
  public Date getCompanyAuditedAt() {
    return companyAuditedAt == null ? null : new Date(companyAuditedAt.getTime());
  }

  public void setCompanyAuditedAt(Date companyAuditedAt) {
    this.companyAuditedAt = companyAuditedAt == null ? null : new Date(companyAuditedAt.getTime());
  }

  // 防御性复制方法 - 系统审核时间
  public Date getSystemAuditedAt() {
    return systemAuditedAt == null ? null : new Date(systemAuditedAt.getTime());
  }

  public void setSystemAuditedAt(Date systemAuditedAt) {
    this.systemAuditedAt = systemAuditedAt == null ? null : new Date(systemAuditedAt.getTime());
  }
}
