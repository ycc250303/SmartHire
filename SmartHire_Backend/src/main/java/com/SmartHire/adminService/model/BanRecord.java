package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户封禁记录表
 * 用于存储用户封禁的详细信息，包括封禁原因、时长、操作管理员等
 *
 * @author SmartHire Team
 * @since 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ban_record")
public class BanRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 封禁记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被封禁的用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户类型：1-求职者 2-HR
     */
    private Byte userType;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁类型：permanent-永久封禁, temporary-临时封禁
     */
    private String banType;

    /**
     * 封禁天数（临时封禁时使用）
     */
    private Integer banDays;

    /**
     * 封禁开始时间
     */
    private Date banStartTime;

    /**
     * 封禁结束时间（null表示永久封禁）
     */
    private Date banEndTime;

    /**
     * 封禁状态：active-生效中, expired-已过期, lifted-已解除
     */
    private String banStatus;

    /**
     * 操作管理员ID
     */
    private Long operatorId;

    /**
     * 操作管理员用户名
     */
    private String operatorName;

    /**
     * 解除封禁的管理员ID
     */
    private Long liftedByOperatorId;

    /**
     * 解除封禁的管理员用户名
     */
    private String liftedByOperatorName;

    /**
     * 解除封禁原因
     */
    private String liftReason;

    /**
     * 解除封禁时间
     */
    private Date liftedAt;

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
    public Date getBanStartTime() {
        return banStartTime == null ? null : new Date(banStartTime.getTime());
    }

    public void setBanStartTime(Date banStartTime) {
        this.banStartTime = banStartTime == null ? null : new Date(banStartTime.getTime());
    }

    public Date getBanEndTime() {
        return banEndTime == null ? null : new Date(banEndTime.getTime());
    }

    public void setBanEndTime(Date banEndTime) {
        this.banEndTime = banEndTime == null ? null : new Date(banEndTime.getTime());
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

    public Date getLiftedAt() {
        return liftedAt == null ? null : new Date(liftedAt.getTime());
    }

    public void setLiftedAt(Date liftedAt) {
        this.liftedAt = liftedAt == null ? null : new Date(liftedAt.getTime());
    }
}