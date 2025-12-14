-- 公司管理员和两级审核功能数据库更新脚本
-- 用于更新现有数据库表结构，支持公司管理员角色和两级审核流程
-- 更新日期: 2025-12-13
-- 
-- 说明：
-- 1. 此脚本用于更新已存在的数据库表结构
-- 2. 如果表或字段已存在，ALTER TABLE 语句会失败，需要手动检查
-- 3. 建议在执行前备份数据库

-- =====================================================
-- 一、更新 hr_info 表
-- =====================================================

-- 添加公司管理员标识字段
ALTER TABLE `hr_info`
ADD COLUMN `is_company_admin` TINYINT DEFAULT 0 COMMENT '是否为公司管理员：0-否 1-是' AFTER `work_phone`;

-- 添加公司管理员相关索引
ALTER TABLE `hr_info`
ADD INDEX `idx_company_admin` (`company_id`, `is_company_admin`);

-- =====================================================
-- 二、更新 job_audit_record 表（如果表已存在）
-- =====================================================

-- 添加公司审核相关字段
ALTER TABLE `job_audit_record`
ADD COLUMN `company_audit_status` VARCHAR(20) COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改' AFTER `audited_at`,
ADD COLUMN `company_auditor_id` BIGINT COMMENT '公司审核员ID' AFTER `company_audit_status`,
ADD COLUMN `company_auditor_name` VARCHAR(50) COMMENT '公司审核员姓名' AFTER `company_auditor_id`,
ADD COLUMN `company_audited_at` DATETIME COMMENT '公司审核时间' AFTER `company_auditor_name`;

-- 添加系统审核相关字段
ALTER TABLE `job_audit_record`
ADD COLUMN `system_audit_status` VARCHAR(20) COMMENT '系统审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改' AFTER `company_audited_at`,
ADD COLUMN `system_auditor_id` BIGINT COMMENT '系统审核员ID' AFTER `system_audit_status`,
ADD COLUMN `system_auditor_name` VARCHAR(50) COMMENT '系统审核员姓名' AFTER `system_auditor_id`,
ADD COLUMN `system_audited_at` DATETIME COMMENT '系统审核时间' AFTER `system_auditor_name`;

-- 添加两级审核相关索引
ALTER TABLE `job_audit_record`
ADD INDEX `idx_company_audit_status` (`company_audit_status`),
ADD INDEX `idx_system_audit_status` (`system_audit_status`),
ADD INDEX `idx_company_auditor_id` (`company_auditor_id`);

-- =====================================================
-- 三、更新 job_info 表
-- =====================================================

-- 添加审核状态字段（如果不存在）
ALTER TABLE `job_info`
ADD COLUMN `audit_status` VARCHAR(20) DEFAULT 'draft' COMMENT '审核状态: draft-草稿 company_pending-待公司审核 system_pending-待系统审核 approved-已通过 rejected-已拒绝 modified-需修改',
ADD COLUMN `company_audit_status` VARCHAR(20) COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
ADD COLUMN `submitted_at` DATETIME COMMENT '提交审核时间',
ADD COLUMN `audited_at` DATETIME COMMENT '审核完成时间';

-- 添加审核相关索引（如果不存在）
ALTER TABLE `job_info`
ADD INDEX `idx_audit_status` (`audit_status`),
ADD INDEX `idx_company_audit_status` (`company_audit_status`),
ADD INDEX `idx_submitted_at` (`submitted_at`),
ADD INDEX `idx_audited_at` (`audited_at`);

-- =====================================================
-- 四、创建 hr_audit_record 表（如果不存在）
-- =====================================================

CREATE TABLE IF NOT EXISTS `hr_audit_record` (
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

-- =====================================================
-- 执行完成
-- =====================================================
-- 
-- 注意事项：
-- 1. 如果某些字段或索引已存在，ALTER TABLE 语句会报错，这是正常的
-- 2. 可以使用以下SQL检查字段是否存在：
--    SHOW COLUMNS FROM `table_name` LIKE 'field_name';
-- 3. 可以使用以下SQL检查索引是否存在：
--    SHOW INDEX FROM `table_name` WHERE Key_name = 'index_name';
-- 4. 建议在生产环境执行前先在测试环境验证

