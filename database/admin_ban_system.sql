-- =====================================================
-- SmartHire 管理员封禁系统数据库表结构
-- 版本: 1.0.0
-- 创建日期: 2025-12-06
-- 描述: 用户封禁管理系统相关表结构
-- =====================================================

USE smarthire;

-- =====================================================
-- 管理员封禁系统表 (1张表)
-- =====================================================

-- 1.1 用户封禁记录表
CREATE TABLE `ban_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '封禁记录ID',
    `user_id` BIGINT NOT NULL COMMENT '被封禁的用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '用户邮箱',
    `user_type` TINYINT NOT NULL COMMENT '用户类型：1-求职者 2-HR',
    `ban_reason` TEXT NOT NULL COMMENT '封禁原因',
    `ban_type` VARCHAR(20) NOT NULL COMMENT '封禁类型：permanent-永久封禁, temporary-临时封禁',
    `ban_days` INT COMMENT '封禁天数（临时封禁时使用）',
    `ban_start_time` DATETIME NOT NULL COMMENT '封禁开始时间',
    `ban_end_time` DATETIME COMMENT '封禁结束时间（null表示永久封禁）',
    `ban_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '封禁状态：active-生效中, expired-已过期, lifted-已解除',
    `operator_id` BIGINT NOT NULL COMMENT '操作管理员ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作管理员用户名',
    `lifted_by_operator_id` BIGINT COMMENT '解除封禁的管理员ID',
    `lifted_by_operator_name` VARCHAR(50) COMMENT '解除封禁的管理员用户名',
    `lift_reason` TEXT COMMENT '解除封禁原因',
    `lifted_at` DATETIME COMMENT '解除封禁时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_active_ban` (`user_id`, `ban_status`) COMMENT '只限制active状态，允许lifted状态重复',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_ban_status` (`ban_status`),
    KEY `idx_ban_end_time` (`ban_end_time`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户封禁记录表';

-- 添加索引优化查询性能
CREATE INDEX `idx_user_ban_lookup` ON `ban_record` (`user_id`, `ban_status`, `ban_end_time`);
CREATE INDEX `idx_operator_time` ON `ban_record` (`operator_id`, `created_at`);