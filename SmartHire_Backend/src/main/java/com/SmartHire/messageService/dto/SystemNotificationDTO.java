package com.SmartHire.messageService.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统消息DTO
 *
 * @author SmartHire Team
 * @since 2025-12-22
 */
@Data
public class SystemNotificationDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型：1-系统消息 2-举报处理 3-封禁通知 4-职位下线 5-警告通知
     */
    private Integer type;

    /**
     * 通知类型名称
     */
    private String typeName;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
}