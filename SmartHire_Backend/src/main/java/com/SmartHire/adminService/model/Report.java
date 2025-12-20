package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;

/**
 * 举报实体类
 * 对应数据库表：reports
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("reports")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 举报ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 举报人ID（关联user表） */
    private Long reporterId;

    /** 举报对象类型：1-用户, 2-职位 */
    private Integer targetType;

    /** 举报对象ID */
    private Long targetId;

    /** 举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他 */
    private Integer reportType;

    /** 举报原因/描述 */
    private String reason;

    /** 处理状态：0-待处理, 1-已处理 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理结果：0-无需处理, 1-警告, 2-封禁, 3-下线 */
    private Integer handleResult;

    /** 处理原因 */
    private String handleReason;

    /** 处理时间 */
    private Date handleTime;

    /** 证据图片URL */
    private String evidenceImage;

    /** 创建时间 */
    @JsonIgnore
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    /** 更新时间 */
    @JsonIgnore
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    // 以下是关联查询时使用的字段（不对应数据库字段）
    @TableField(exist = false)
    private String reporterName;

    @TableField(exist = false)
    private Integer reporterType;

    @TableField(exist = false)
    private String targetTitle;

    // 枚举常量
    /** 举报对象类型 */
    public static class TargetType {
        public static final int USER = 1;
        public static final int JOB = 2;
    }

    /** 举报类型 */
    public static class ReportType {
        public static final int SPAM = 1;          // 垃圾信息
        public static final int INAPPROPRIATE = 2;  // 不当内容
        public static final int FAKE_JOB = 3;       // 虚假职位
        public static final int FRAUD = 4;          // 欺诈行为
        public static final int HARASSMENT = 5;     // 骚扰行为
        public static final int OTHER = 6;          // 其他
    }

    /** 处理状态 */
    public static class Status {
        public static final int PENDING = 0;        // 待处理
        public static final int RESOLVED = 1;       // 已处理
    }

    /** 处理结果 */
    public static class HandleResult {
        public static final int NO_ACTION = 0;      // 无需处理
        public static final int WARN = 1;           // 警告
        public static final int BAN = 2;            // 封禁
        public static final int OFFLINE = 3;        // 下线
    }

    // 防御性拷贝Date字段
    public Date getHandleTime() {
        return handleTime != null ? new Date(handleTime.getTime()) : null;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime != null ? new Date(handleTime.getTime()) : null;
    }

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }
}