package com.SmartHire.common.dto.messageDto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模块间通用的系统通知信息
 */
@Data
public class SystemNotificationCommonDTO implements Serializable {
    private Long id;
    private Integer type;
    private String typeName;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /**
     * 通知类型常量
     */
    public static class Type {
        public static final int SYSTEM_MESSAGE = 1; // 系统消息
        public static final int REPORT_RESULT = 2; // 举报处理
        public static final int USER_BAN = 3; // 封禁通知
        public static final int JOB_OFFLINE = 4; // 职位下线
        public static final int USER_WARNING = 5; // 警告通知
    }
}
