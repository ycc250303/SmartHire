-- ========================================
-- SmartHire 举报功能数据库表结构
-- 创建时间: 2025-12-12
-- 描述: 创建举报主表
-- ========================================

USE smarthire;

-- 创建举报主表
CREATE TABLE `reports` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID（关联user表）',
  `target_type` TINYINT NOT NULL COMMENT '举报对象类型：1-用户, 2-职位',
  `target_id` BIGINT NOT NULL COMMENT '举报对象ID',
  `report_type` TINYINT NOT NULL COMMENT '举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他',
  `reason` TEXT NOT NULL COMMENT '举报原因/详细描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-已处理',
  `handler_id` BIGINT NULL COMMENT '处理人ID（管理员ID）',
  `handle_result` TINYINT NULL COMMENT '处理结果：0-无需处理, 1-警告, 2-封禁/下线',
  `handle_reason` TEXT NULL COMMENT '处理原因',
  `handle_time` DATETIME NULL COMMENT '处理时间',
  `evidence_image` LONGTEXT NULL COMMENT '举报证据图片（base64编码，支持jpg/pdf）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_reporter_id` (`reporter_id`),
  INDEX `idx_target` (`target_type`, `target_id`),
  INDEX `idx_report_type` (`report_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_handler_id` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报信息主表';