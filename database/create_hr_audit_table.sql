-- HR审核记录表创建脚本
-- 用于公司管理员审核HR的功能
-- 创建日期: 2025-12-13

-- HR审核记录表
CREATE TABLE `hr_audit_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    `hr_id` BIGINT NOT NULL COMMENT '被审核HR ID',
    `user_id` BIGINT NOT NULL COMMENT '被审核HR的用户ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_name` VARCHAR(50) NOT NULL COMMENT 'HR姓名',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已拒绝',
    `auditor_id` BIGINT COMMENT '审核员ID（公司管理员）',
    `auditor_name` VARCHAR(50) COMMENT '审核员姓名',
    `audit_reason` TEXT COMMENT '审核意见',
    `reject_reason` TEXT COMMENT '拒绝原因',
    `audited_at` DATETIME COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_hr_id` (`hr_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR审核记录表';

