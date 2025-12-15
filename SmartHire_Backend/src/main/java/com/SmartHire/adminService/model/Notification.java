package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体类
 *
 * @author SmartHire Team
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notifications")
public class Notification {

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知类型：1-系统消息 2-举报处理 3-封禁通知 4-职位下线 5-警告通知
     */
    @TableField("type")
    private Integer type;

    /**
     * 通知标题（可选）
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 关联业务ID（举报ID、职位ID、用户ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联业务类型：report, job, user, system
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 是否已读：0-未读 1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    @TableField("read_at")
    private LocalDateTime readAt;

    /**
     * 通知类型常量
     */
    public static class Type {
        public static final int SYSTEM_MESSAGE = 1;    // 系统消息
        public static final int REPORT_RESULT = 2;     // 举报处理
        public static final int USER_BAN = 3;          // 封禁通知
        public static final int JOB_OFFLINE = 4;       // 职位下线
        public static final int USER_WARNING = 5;      // 警告通知
    }

    /**
     * 是否已读状态常量
     */
    public static class ReadStatus {
        public static final int UNREAD = 0;    // 未读
        public static final int READ = 1;      // 已读
    }

    /**
     * 关联业务类型常量
     */
    public static class RelatedType {
        public static final String REPORT = "report";
        public static final String JOB = "job";
        public static final String USER = "user";
        public static final String SYSTEM = "system";
    }
}