-- 职位审核系统数据库脚本
-- 创建岗位审核功能所需的数据表结构
-- 版本: 2.0 - 支持两级审核（公司审核 + 系统审核）
-- 更新日期: 2025-12-13

-- 职位审核记录表（包含两级审核字段）
CREATE TABLE `job_audit_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '审核记录ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `job_title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_id` BIGINT NOT NULL COMMENT 'HR ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `hr_name` VARCHAR(50) NOT NULL COMMENT 'HR姓名',
    `audit_note` TEXT COMMENT '审核备注',
    `audit_reason` TEXT COMMENT '审核意见（要求修改时使用）',
    `reject_reason` TEXT COMMENT '拒绝原因',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `auditor_id` BIGINT COMMENT '审核员ID（兼容字段）',
    `auditor_name` VARCHAR(50) COMMENT '审核员姓名（兼容字段）',
    `audited_at` DATETIME COMMENT '审核时间（兼容字段）',
    -- 公司审核相关字段
    `company_audit_status` VARCHAR(20) COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `company_auditor_id` BIGINT COMMENT '公司审核员ID',
    `company_auditor_name` VARCHAR(50) COMMENT '公司审核员姓名',
    `company_audited_at` DATETIME COMMENT '公司审核时间',
    -- 系统审核相关字段
    `system_audit_status` VARCHAR(20) COMMENT '系统审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `system_auditor_id` BIGINT COMMENT '系统审核员ID',
    `system_auditor_name` VARCHAR(50) COMMENT '系统审核员姓名',
    `system_audited_at` DATETIME COMMENT '系统审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_created_at` (`created_at`),
    -- 两级审核相关索引
    KEY `idx_company_audit_status` (`company_audit_status`),
    KEY `idx_system_audit_status` (`system_audit_status`),
    KEY `idx_company_auditor_id` (`company_auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位审核记录表（支持两级审核）';

-- 扩展职位表，添加审核相关字段
ALTER TABLE `job_info`
ADD COLUMN `audit_status` VARCHAR(20) DEFAULT 'draft' COMMENT '审核状态: draft-草稿 company_pending-待公司审核 system_pending-待系统审核 approved-已通过 rejected-已拒绝 modified-需修改',
ADD COLUMN `company_audit_status` VARCHAR(20) COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
ADD COLUMN `submitted_at` DATETIME COMMENT '提交审核时间',
ADD COLUMN `audited_at` DATETIME COMMENT '审核完成时间';

-- 添加索引优化查询性能
ALTER TABLE `job_info`
ADD INDEX `idx_audit_status` (`audit_status`),
ADD INDEX `idx_company_audit_status` (`company_audit_status`),
ADD INDEX `idx_submitted_at` (`submitted_at`),
ADD INDEX `idx_audited_at` (`audited_at`);

-- 注意：测试数据需要根据实际的job_info表数据来创建
-- 这里提供插入测试数据的模板，请根据实际数据调整job_id和其他字段
--
-- INSERT INTO `job_audit_record` (
--     `job_id`, `job_title`, `company_id`, `hr_id`, `company_name`, `hr_name`,
--     `audit_status`, `auditor_id`, `auditor_name`, `created_at`
-- )
-- SELECT
--     id, job_title, company_id, hr_id,
--     (SELECT company_name FROM company WHERE id = job_info.company_id) as company_name,
--     (SELECT real_name FROM hr_info WHERE id = job_info.hr_id) as hr_name,
--     'pending' as audit_status, NULL, NULL, NOW()
-- FROM job_info
-- WHERE id IN (1, 2, 3, 4);