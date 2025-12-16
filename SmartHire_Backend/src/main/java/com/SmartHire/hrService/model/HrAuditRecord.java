package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * HR审核记录实体类
 *
 * @author SmartHire Team
 * @since 2025-12-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hr_audit_record")
public class HrAuditRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被审核HR ID
     */
    @TableField("hr_id")
    private Long hrId;

    /**
     * 被审核HR的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * HR姓名
     */
    @TableField("hr_name")
    private String hrName;

    /**
     * 审核状态: pending-待审核 approved-已通过 rejected-已拒绝
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 审核员ID（公司管理员）
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核员姓名
     */
    @TableField("auditor_name")
    private String auditorName;

    /**
     * 审核意见
     */
    @TableField("audit_reason")
    private String auditReason;

    /**
     * 拒绝原因
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 审核时间
     */
    @TableField("audited_at")
    private Date auditedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

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
}

