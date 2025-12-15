-- 通知系统数据库表结构
-- 创建时间：2025-12-15
-- 描述：用户通知系统的核心表结构

-- 用户通知表
CREATE TABLE `notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` TINYINT NOT NULL COMMENT '通知类型：1-系统消息 2-举报处理 3-封禁通知 4-职位下线 5-警告通知',
    `title` VARCHAR(255) COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `related_id` BIGINT COMMENT '关联业务ID（举报ID、职位ID、用户ID等）',
    `related_type` VARCHAR(50) COMMENT '关联业务类型：report, job, user, system',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_at` DATETIME COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type_status` (`type`, `is_read`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_related` (`related_type`, `related_id`),
    KEY `idx_user_unread` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知表';