-- =====================================================
-- SmartHire 智慧招聘系统数据库表结构
-- 版本: v2.0
-- 更新日期: 2025-12-30
-- 描述: 系统所有数据表结构，按功能模块划分
-- =====================================================
-- ==============================================
-- 基础模块：用户与权限
-- ==============================================
-- 用户基础表
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT COMMENT '用户ID' PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `user_type` TINYINT NOT NULL COMMENT '用户类型：1-求职者 2-HR',
    `status` TINYINT DEFAULT 1 NULL COMMENT '账户状态：0-禁用 1-正常',
    `avatar_url` VARCHAR(255) NULL COMMENT '头像URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` DATETIME NULL COMMENT '最后登录时间',
    `gender` TINYINT DEFAULT 2 NOT NULL COMMENT '性别 0-男 1-女 2-不愿透露',
    CONSTRAINT uk_email UNIQUE (email),
    CONSTRAINT uk_phone UNIQUE (phone),
    CONSTRAINT uk_username UNIQUE (username)
) COMMENT '用户基础表' CHARSET = utf8mb4;
CREATE INDEX idx_user_type ON user (user_type);
-- 求职者信息表
CREATE TABLE `job_seeker` (
    `id` BIGINT AUTO_INCREMENT COMMENT '求职者ID' PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `birth_date` DATE NULL COMMENT '出生日期',
    `current_city` VARCHAR(50) NULL COMMENT '当前城市',
    `education` TINYINT NULL COMMENT '最高学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士',
    `job_status` TINYINT NULL COMMENT '求职状态 0-离校-尽快到岗 1-在校-尽快到岗 2-在校-考虑机会 3-在校-暂不考虑',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `graduation_year` VARCHAR(10) NULL COMMENT '毕业年份',
    `work_experience_year` INT DEFAULT 0 NOT NULL COMMENT '工作经验年份',
    `internship_experience` TINYINT DEFAULT 0 NOT NULL COMMENT '是否有实习经历',
    CONSTRAINT uk_user_id UNIQUE (user_id)
) COMMENT '求职者信息表' CHARSET = utf8mb4;
CREATE INDEX idx_city ON job_seeker (current_city);
CREATE INDEX idx_education ON job_seeker (education);
-- 求职者期望表
CREATE TABLE `job_seeker_expectation` (
    `id` BIGINT AUTO_INCREMENT COMMENT '求职期望ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `expected_position` VARCHAR(100) NOT NULL COMMENT '期望职位',
    `expected_industry` VARCHAR(100) NULL COMMENT '期望行业',
    `work_city` VARCHAR(50) NOT NULL COMMENT '期望工作城市',
    `salary_min` DECIMAL(10, 2) NULL COMMENT '期望薪资最低',
    `salary_max` DECIMAL(10, 2) NULL COMMENT '期望薪资最高',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '求职者期望表' CHARSET = utf8mb4;
CREATE INDEX idx_city_status ON job_seeker_expectation (work_city);
CREATE INDEX idx_industry ON job_seeker_expectation (expected_industry);
CREATE INDEX idx_job_seeker_id ON job_seeker_expectation (job_seeker_id);
CREATE INDEX idx_salary_max ON job_seeker_expectation (salary_max);
CREATE INDEX idx_salary_min ON job_seeker_expectation (salary_min);
-- HR信息表
CREATE TABLE `hr_info` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'HR ID' PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `position` VARCHAR(50) NULL COMMENT '职位',
    `work_phone` VARCHAR(20) NULL COMMENT '工作电话',
    `is_company_admin` TINYINT DEFAULT 0 NULL COMMENT '是否为公司管理员：0-否 1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_user_id UNIQUE (user_id)
) COMMENT 'HR信息表' CHARSET = utf8mb4;
CREATE INDEX idx_company_admin ON hr_info (company_id, is_company_admin);
CREATE INDEX idx_company_id ON hr_info (company_id);
-- HR审核记录表
CREATE TABLE `hr_audit_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '审核记录ID' PRIMARY KEY,
    `hr_id` BIGINT NOT NULL COMMENT '被审核HR ID',
    `user_id` BIGINT NOT NULL COMMENT '被审核HR的用户ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_name` VARCHAR(50) NOT NULL COMMENT 'HR姓名',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已拒绝',
    `auditor_id` BIGINT NULL COMMENT '审核员ID（公司管理员）',
    `auditor_name` VARCHAR(50) NULL COMMENT '审核员姓名',
    `audit_reason` TEXT NULL COMMENT '审核意见',
    `reject_reason` TEXT NULL COMMENT '拒绝原因',
    `audited_at` DATETIME NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT 'HR审核记录表' CHARSET = utf8mb4;
CREATE INDEX idx_audit_status ON hr_audit_record (audit_status);
CREATE INDEX idx_auditor_id ON hr_audit_record (auditor_id);
CREATE INDEX idx_company_id ON hr_audit_record (company_id);
CREATE INDEX idx_hr_id ON hr_audit_record (hr_id);
CREATE INDEX idx_user_id ON hr_audit_record (user_id);
-- 用户封禁记录表
CREATE TABLE `ban_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '封禁记录ID' PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '被封禁的用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '用户邮箱',
    `user_type` TINYINT NOT NULL COMMENT '用户类型：1-求职者 2-HR',
    `ban_reason` TEXT NOT NULL COMMENT '封禁原因',
    `ban_type` VARCHAR(20) NOT NULL COMMENT '封禁类型：permanent-永久封禁, temporary-临时封禁',
    `ban_days` INT NULL COMMENT '封禁天数（临时封禁时使用）',
    `ban_start_time` DATETIME NOT NULL COMMENT '封禁开始时间',
    `ban_end_time` DATETIME NULL COMMENT '封禁结束时间（null表示永久封禁）',
    `ban_status` VARCHAR(20) DEFAULT 'active' NOT NULL COMMENT '封禁状态：active-生效中, expired-已过期, lifted-已解除',
    `operator_id` BIGINT NOT NULL COMMENT '操作管理员ID',
    `operator_name` VARCHAR(50) NOT NULL COMMENT '操作管理员用户名',
    `lifted_by_operator_id` BIGINT NULL COMMENT '解除封禁的管理员ID',
    `lifted_by_operator_name` VARCHAR(50) NULL COMMENT '解除封禁的管理员用户名',
    `lift_reason` TEXT NULL COMMENT '解除封禁原因',
    `lifted_at` DATETIME NULL COMMENT '解除封禁时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户封禁记录表' CHARSET = utf8mb4;
CREATE INDEX idx_ban_end_time ON ban_record (ban_end_time);
CREATE INDEX idx_ban_status ON ban_record (ban_status);
CREATE INDEX idx_created_at ON ban_record (created_at);
CREATE INDEX idx_operator_id ON ban_record (operator_id);
CREATE INDEX idx_operator_time ON ban_record (operator_id, created_at);
CREATE INDEX idx_user_ban_lookup ON ban_record (user_id, ban_status, ban_end_time);
CREATE INDEX idx_user_id ON ban_record (user_id);
-- ==============================================
-- 公司与职位模块
-- ==============================================
-- 公司信息表
CREATE TABLE `company` (
    `id` BIGINT AUTO_INCREMENT COMMENT '公司ID' PRIMARY KEY,
    `owner_user_id` bigint not null comment '公司老板的用户id',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `industry` VARCHAR(50) NULL COMMENT '所属行业',
    `company_scale` TINYINT NULL COMMENT '公司规模 1:0-20 2：20-99 3:100-499 4:500-999 5:1000-3000 6:3000-10000 7:10000以上',
    `financing_stage` TINYINT NULL COMMENT '融资阶段 0:无融资 1:天使轮 2:A轮 3:B轮 4:C轮 5:D轮',
    `website` VARCHAR(255) NULL COMMENT '公司网站',
    `logo_url` VARCHAR(255) NULL COMMENT '公司Logo',
    `description` TEXT NULL COMMENT '公司简介',
    `benefits` TEXT NULL COMMENT '福利待遇',
    `status` TINYINT DEFAULT 1 NULL COMMENT '状态：0-未认证 1-已认证',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `company_created_at` DATETIME NOT NULL COMMENT '公司创建时间',
    `registered_capital` INT NOT NULL COMMENT '注册资本（万元）',
    `audit_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态',
    `audited_at` DATETIME NULL COMMENT '审核时间'
) COMMENT '公司信息表' CHARSET = utf8mb4;
CREATE INDEX idx_company_name ON company (company_name);
CREATE INDEX idx_industry ON company (industry);
CREATE TABLE `company_audit_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '审核记录ID' PRIMARY KEY,
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `auditor_id` BIGINT NULL COMMENT '审核员ID（系统管理员）',
    `auditor_name` VARCHAR(50) NULL COMMENT '审核员姓名',
    `audit_reason` TEXT NULL COMMENT '审核意见',
    `reject_reason` TEXT NULL COMMENT '拒绝原因',
    `audited_at` DATETIME NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '公司审核记录表' CHARSET = utf8mb4;
CREATE INDEX idx_audit_status ON company_audit_record (audit_status);
CREATE INDEX idx_auditor_id ON company_audit_record (auditor_id);
CREATE INDEX idx_company_id ON company_audit_record (company_id);
-- 职位表
CREATE TABLE `job_info` (
    `id` BIGINT AUTO_INCREMENT COMMENT '职位ID' PRIMARY KEY,
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_id` BIGINT NOT NULL COMMENT '发布HR的ID',
    `job_title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `job_category` VARCHAR(50) NULL COMMENT '职位类别',
    `department` VARCHAR(50) NULL COMMENT '部门',
    `city` VARCHAR(50) NOT NULL COMMENT '工作城市',
    `address` VARCHAR(255) NULL COMMENT '详细地址',
    `salary_min` DECIMAL(10, 2) NULL COMMENT '薪资最低',
    `salary_max` DECIMAL(10, 2) NULL COMMENT '薪资最高',
    `salary_months` INT DEFAULT 12 NULL COMMENT '薪资月数',
    `education_required` TINYINT NULL COMMENT '学历要求 0-不限 1-专科 2-本科 3-硕士 4-博士',
    `job_type` TINYINT NULL COMMENT '工作类型 0-全职 1-实习',
    `description` TEXT NOT NULL COMMENT '职位描述',
    `responsibilities` TEXT NULL COMMENT '岗位职责',
    `requirements` TEXT NULL COMMENT '任职要求',
    `status` TINYINT DEFAULT 1 NULL COMMENT '状态：0-已下线 1-招聘中 2-已暂停',
    `view_count` INT DEFAULT 0 NULL COMMENT '浏览次数',
    `application_count` INT DEFAULT 0 NULL COMMENT '申请次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_at` DATETIME NULL COMMENT '发布时间',
    `internship_days_per_week` INT NULL COMMENT '每周实习天数（仅实习类型需要）',
    `internship_duration_months` INT NULL COMMENT '实习时长（月为单位，仅实习类型需要）',
    `experience_required` TINYINT NULL COMMENT '经验要求（仅全职类型需要）0-应届生 1-1年以内 2-1-3年 3-3-5年 4-5-10年 5-10年以上',
    `audit_status` VARCHAR(20) DEFAULT 'draft' NULL COMMENT '审核状态: draft-草稿 pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `company_audit_status` VARCHAR(20) NULL COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `submitted_at` DATETIME NULL COMMENT '提交审核时间',
    `audited_at` DATETIME NULL COMMENT '审核完成时间'
) COMMENT '职位表' CHARSET = utf8mb4;
CREATE INDEX idx_audit_status ON job_info (audit_status);
CREATE INDEX idx_audited_at ON job_info (audited_at);
CREATE INDEX idx_city_status ON job_info (city, status);
CREATE INDEX idx_company_audit_status ON job_info (company_audit_status);
CREATE INDEX idx_company_id ON job_info (company_id);
CREATE INDEX idx_hr_id ON job_info (hr_id);
CREATE INDEX idx_published_at ON job_info (published_at);
CREATE INDEX idx_submitted_at ON job_info (submitted_at);
-- 职位审核记录表
CREATE TABLE `job_audit_record` (
    `id` BIGINT AUTO_INCREMENT COMMENT '审核记录ID' PRIMARY KEY,
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `job_title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_id` BIGINT NOT NULL COMMENT 'HR ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `hr_name` VARCHAR(50) NOT NULL COMMENT 'HR姓名',
    `audit_note` TEXT NULL COMMENT '审核备注',
    `audit_reason` TEXT NULL COMMENT '审核意见（要求修改时使用）',
    `reject_reason` TEXT NULL COMMENT '拒绝原因',
    `audit_status` VARCHAR(20) NOT NULL COMMENT '审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `company_audit_status` VARCHAR(20) NULL COMMENT '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `company_auditor_id` BIGINT NULL COMMENT '公司审核员ID',
    `company_auditor_name` VARCHAR(50) NULL COMMENT '公司审核员姓名',
    `company_audited_at` DATETIME NULL COMMENT '公司审核时间',
    `system_audit_status` VARCHAR(20) NULL COMMENT '系统审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    `system_auditor_id` BIGINT NULL COMMENT '系统审核员ID',
    `system_auditor_name` VARCHAR(50) NULL COMMENT '系统审核员姓名',
    `system_audited_at` DATETIME NULL COMMENT '系统审核时间',
    `auditor_id` BIGINT NULL COMMENT '审核员ID',
    `auditor_name` VARCHAR(50) NULL COMMENT '审核员姓名',
    `audited_at` DATETIME NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '职位审核记录表' CHARSET = utf8mb4;
CREATE INDEX idx_audit_status ON job_audit_record (audit_status);
CREATE INDEX idx_auditor_id ON job_audit_record (auditor_id);
CREATE INDEX idx_company_audit_status ON job_audit_record (company_audit_status);
CREATE INDEX idx_company_auditor_id ON job_audit_record (company_auditor_id);
CREATE INDEX idx_created_at ON job_audit_record (created_at);
CREATE INDEX idx_job_id ON job_audit_record (job_id);
CREATE INDEX idx_system_audit_status ON job_audit_record (system_audit_status);
-- 职位技能要求表
CREATE TABLE `job_skill_requirement` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `skill_name` VARCHAR(50) NOT NULL COMMENT '技能名称',
    `is_required` TINYINT DEFAULT 1 NULL COMMENT '是否必需：0-加分项 1-必须',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间'
) COMMENT '职位技能要求表' CHARSET = utf8mb4;
CREATE INDEX idx_job_id ON job_skill_requirement (job_id);
CREATE INDEX idx_skill_name ON job_skill_requirement (skill_name);
-- ==============================================
-- 简历模块
-- ==============================================
-- 简历基础表
CREATE TABLE `resume` (
    `id` BIGINT AUTO_INCREMENT COMMENT '简历ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `resume_name` VARCHAR(100) NOT NULL COMMENT '简历名称',
    `privacy_level` TINYINT DEFAULT 1 NULL COMMENT '隐私级别：1-公开 2-仅投递可见',
    `file_url` VARCHAR(255) NULL COMMENT '简历文件URL',
    `completeness` INT DEFAULT 0 NULL COMMENT '完整度（0-100）(AI评估）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '简历基础表' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON resume (job_seeker_id);
-- 工作经历表（在线简历信息）
CREATE TABLE `work_experience` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `position` VARCHAR(100) NOT NULL COMMENT '职位',
    `department` VARCHAR(50) NULL COMMENT '部门（可选）',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NULL COMMENT '结束日期',
    `is_internship` TINYINT DEFAULT 0 NULL COMMENT '是否实习 0-不是 1-是',
    `description` TEXT NULL COMMENT '工作内容',
    `achievements` TEXT NULL COMMENT '工作成果',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '工作经历表（在线简历信息）' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON work_experience (job_seeker_id);
-- 教育经历表（在线简历信息）
CREATE TABLE `education_experience` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `school_name` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `major` VARCHAR(100) NULL COMMENT '专业',
    `education` TINYINT NULL COMMENT '学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士',
    `start_year` DATE NOT NULL COMMENT '开始年份',
    `end_year` DATE NULL COMMENT '结束年份',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '教育经历表（在线简历信息）' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON education_experience (job_seeker_id);
-- 项目经历表（在线简历信息）
CREATE TABLE `project_experience` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `project_role` VARCHAR(50) NULL COMMENT '项目角色',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NULL COMMENT '结束日期',
    `description` TEXT NULL COMMENT '项目描述',
    `responsibility` TEXT NULL COMMENT '职责描述',
    `achievement` TEXT NULL COMMENT '项目成果',
    `project_url` VARCHAR(255) NULL COMMENT '项目链接',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '项目经历表（在线简历信息）' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON project_experience (job_seeker_id);
-- 技能表（在线简历信息）
CREATE TABLE `skill` (
    `id` BIGINT AUTO_INCREMENT COMMENT '技能ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `skill_name` VARCHAR(50) NOT NULL COMMENT '技能名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `level` TINYINT NULL COMMENT '技能熟练度 0-了解 1-熟悉 2-掌握',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '技能表（在线简历信息）' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON skill (job_seeker_id);
CREATE INDEX idx_skill_name ON skill (skill_name);
-- 证书表（在线简历信息）
CREATE TABLE `certificate` (
    `id` BIGINT AUTO_INCREMENT COMMENT '证书ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `certificate_name` VARCHAR(100) NOT NULL COMMENT '证书名称',
    `certificate_url` VARCHAR(255) NULL COMMENT '证书图片URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间'
) COMMENT '证书表（在线简历信息）' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON certificate (job_seeker_id);
-- ==============================================
-- 职位管理模块
-- ==============================================
-- 岗位筛选表
CREATE TABLE `job_filter` (
    `id` BIGINT AUTO_INCREMENT COMMENT '筛选ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `job_type` TINYINT NOT NULL COMMENT '岗位类型：0-全职 1-实习',
    `education_required` TINYINT DEFAULT 0 NULL COMMENT '学历要求：0-不限 1-高中及以下 2-专科 3-本科 4-硕士 5-博士',
    `company_scale` TINYINT NULL COMMENT '公司规模：NULL-不限 1:0-20 2:20-99 3:100-499 4:500-999 5:1000-3000 6:3000-10000 7:10000以上',
    `financing_stage` TINYINT NULL COMMENT '融资阶段：NULL-不限 0:无融资 1:天使轮 2:A轮 3:B轮 4:C轮 5:D轮 6:已上市',
    `internship_duration` TINYINT DEFAULT 0 NULL COMMENT '实习时长（仅实习类型）：0-不限 1-1个月 2-1-3个月 3-3-6个月 4-6个月以上',
    `internship_days_per_week` TINYINT DEFAULT 0 NULL COMMENT '每周出勤天数（仅实习类型）：0-不限 1-1天 2-2天 3-3天 4-4天 5-5天',
    `internship_salary_range` TINYINT DEFAULT 0 NULL COMMENT '实习日薪范围（仅实习类型）：0-不限 1-100以下 2-100-200 3-200-300 4-300-400 5-400-500 6-500以上',
    `fulltime_salary_range` TINYINT DEFAULT 0 NULL COMMENT '全职月薪范围（仅全职类型）：0-不限 1-3k以下 2-3-5k 3-5-10k 4-10-15k 5-15-20k 6-20-30k 7-30-50k 8-50k以上',
    `experience_required` TINYINT DEFAULT 0 NULL COMMENT '经验要求（仅全职类型）：0-不限 1-应届生 2-1年以内 3-1-3年 4-3-5年 5-5-10年 6-10年以上',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '岗位筛选表' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON job_filter (job_seeker_id);
CREATE INDEX idx_job_type ON job_filter (job_type);
-- 职位收藏表
CREATE TABLE `job_favorite` (
    `id` BIGINT AUTO_INCREMENT COMMENT '收藏ID' PRIMARY KEY,
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '收藏时间',
    CONSTRAINT uk_seeker_job UNIQUE (job_seeker_id, job_id)
) COMMENT '职位收藏表' CHARSET = utf8mb4;
-- 候选人收藏表
CREATE TABLE `candidate_favorite` (
    `id` BIGINT AUTO_INCREMENT COMMENT '收藏ID' PRIMARY KEY,
    `hr_id` BIGINT NOT NULL COMMENT 'HR ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '收藏时间',
    CONSTRAINT uk_hr_seeker UNIQUE (hr_id, job_seeker_id)
) COMMENT '候选人收藏表' CHARSET = utf8mb4;
-- ==============================================
-- 投递与面试模块
-- ==============================================
-- 投递/推荐记录表
CREATE TABLE `application` (
    `id` BIGINT AUTO_INCREMENT COMMENT '记录ID' PRIMARY KEY,
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `resume_id` BIGINT NULL COMMENT '简历ID（投递时必填，推荐时可为空）',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `initiator` TINYINT NOT NULL COMMENT '发起方：0-求职者投递 1-HR推荐',
    `status` TINYINT DEFAULT 0 NULL COMMENT '状态：
        0-已投递/已推荐
        1-已查看
        2-待面试
        3-已面试
        4-已录用
        5-已拒绝
        6-已撤回',
    `match_score` DECIMAL(5, 2) NULL COMMENT '匹配度分数（0-100）',
    `match_analysis` TEXT NULL COMMENT '匹配分析（JSON格式）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_job_seeker_job UNIQUE (job_id, job_seeker_id)
) COMMENT '投递/推荐记录表' CHARSET = utf8mb4;
CREATE INDEX idx_job_seeker_id ON application (job_seeker_id);
CREATE INDEX idx_status ON application (status);
-- 面试安排表
CREATE TABLE `interview` (
    `id` BIGINT AUTO_INCREMENT COMMENT '面试ID' PRIMARY KEY,
    `application_id` BIGINT NOT NULL COMMENT '投递记录ID',
    `message_id` BIGINT NOT NULL COMMENT '消息ID',
    `interview_type` TINYINT NOT NULL COMMENT '面试类型：1-电话 2-视频 3-现场',
    `interview_round` INT DEFAULT 1 NULL COMMENT '面试轮次',
    `interview_time` DATETIME NOT NULL COMMENT '面试时间',
    `duration` INT NULL COMMENT '时长（分钟）',
    `location` VARCHAR(255) NULL COMMENT '面试地点',
    `meeting_link` VARCHAR(255) NULL COMMENT '视频链接',
    `interviewer` VARCHAR(100) NULL COMMENT '面试官',
    `status` TINYINT DEFAULT 0 NULL COMMENT '状态：0-待确认 1-已确认 2-已取消',
    `feedback` TEXT NULL COMMENT '面试反馈',
    `result` TINYINT NULL COMMENT '面试结果：0-未通过 1-通过',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '面试安排表' CHARSET = utf8mb4;
CREATE INDEX idx_application_id ON interview (application_id);
CREATE INDEX idx_interview_time ON interview (interview_time);
CREATE INDEX idx_status ON interview (status);
-- Offer表（录用通知书）
CREATE TABLE `offer` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'Offer ID' PRIMARY KEY,
    `application_id` BIGINT NOT NULL COMMENT '投递记录ID',
    `message_id` BIGINT NOT NULL COMMENT '消息ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `hr_id` BIGINT NOT NULL COMMENT 'HR ID',
    `base_salary` DECIMAL(10, 2) NULL COMMENT '基本薪资',
    `start_date` DATE NULL COMMENT '到岗日期',
    `status` TINYINT DEFAULT 0 NOT NULL COMMENT 'Offer状态：0-待接受 1-已接受 2-已拒绝',
    `accepted_at` DATETIME NULL COMMENT '接受时间',
    `rejected_at` DATETIME NULL COMMENT '拒绝时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT 'Offer表（录用通知书）' CHARSET = utf8mb4;
CREATE INDEX idx_application_id ON offer (application_id);
CREATE INDEX idx_job_seeker_id ON offer (job_seeker_id);
CREATE INDEX idx_hr_id ON offer (hr_id);
CREATE INDEX idx_status ON offer (status);
-- ==============================================
-- 聊天与通知模块
-- ==============================================
-- 一对一会话表
CREATE TABLE `conversation` (
    `id` BIGINT AUTO_INCREMENT COMMENT '会话ID' PRIMARY KEY,
    `user1_id` BIGINT NOT NULL COMMENT '用户A',
    `user2_id` BIGINT NOT NULL COMMENT '用户B',
    `last_message` TEXT NULL COMMENT '最近一条消息内容预览',
    `last_message_time` DATETIME NULL COMMENT '最近消息时间',
    `unread_count_user1` INT DEFAULT 0 NULL,
    `unread_count_user2` INT DEFAULT 0 NULL,
    `pinned_by_user1` TINYINT DEFAULT 0 NULL,
    `pinned_by_user2` TINYINT DEFAULT 0 NULL,
    `has_notification_user1` TINYINT DEFAULT 0 NULL COMMENT '用户1是否有未读通知',
    `has_notification_user2` TINYINT DEFAULT 0 NULL COMMENT '用户2是否有未读通知',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL,
    `deleted_by_user1` TINYINT DEFAULT 0 NULL,
    `deleted_by_user2` TINYINT DEFAULT 0 NULL,
    CONSTRAINT uniq_conversation_pair UNIQUE (user1_id, user2_id)
) COMMENT '一对一会话' CHARSET = utf8mb4;
-- 聊天消息表
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT COMMENT '消息ID' PRIMARY KEY,
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `message_type` TINYINT DEFAULT 1 NULL COMMENT '消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 8-面试邀请 9-Offer 10-拒绝通知',
    `content` TEXT NULL COMMENT '消息内容',
    `file_url` VARCHAR(255) NULL COMMENT '文件/图片/语音/视频URL',
    `reply_to` BIGINT NULL COMMENT '引用的消息ID',
    `is_read` TINYINT DEFAULT 0 NULL COMMENT '是否已读',
    `is_flagged` TINYINT DEFAULT 0 NULL COMMENT '是否被标记为敏感',
    `is_deleted` TINYINT DEFAULT 0 NULL COMMENT '是否被逻辑删除/撤回',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '发送时间'
) COMMENT '聊天消息表' CHARSET = utf8mb4;
CREATE INDEX idx_conversation_created ON chat_message (conversation_id, created_at);
CREATE INDEX idx_conversation_id ON chat_message (conversation_id);
CREATE INDEX idx_created_at ON chat_message (created_at);
CREATE INDEX idx_sender_receiver ON chat_message (sender_id, receiver_id);
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
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户通知表';
-- ==============================================
-- 管理模块
-- ==============================================
-- 举报信息主表
CREATE TABLE `reports` (
    `id` BIGINT AUTO_INCREMENT COMMENT '举报ID' PRIMARY KEY,
    `reporter_id` BIGINT NOT NULL COMMENT '举报人ID（关联user表）',
    `target_type` TINYINT NOT NULL COMMENT '举报对象类型：1-用户, 2-职位',
    `target_id` BIGINT NOT NULL COMMENT '举报对象ID',
    `report_type` TINYINT NOT NULL COMMENT '举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他',
    `reason` TEXT NOT NULL COMMENT '举报原因/详细描述',
    `status` TINYINT DEFAULT 0 NOT NULL COMMENT '处理状态：0-待处理, 1-已处理',
    `handler_id` BIGINT NULL COMMENT '处理人ID（管理员ID）',
    `handle_result` TINYINT NULL COMMENT '处理结果：0-无需处理, 1-警告, 2-封禁/下线',
    `handle_reason` TEXT NULL COMMENT '处理原因',
    `handle_time` DATETIME NULL COMMENT '处理时间',
    `evidence_image` LONGTEXT NULL COMMENT '举报证据图片（base64编码，支持jpg/pdf）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '举报信息主表' CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_created_at ON reports (created_at);
CREATE INDEX idx_handler_id ON reports (handler_id);
CREATE INDEX idx_report_type ON reports (report_type);
CREATE INDEX idx_reporter_id ON reports (reporter_id);
CREATE INDEX idx_status ON reports (status);
CREATE INDEX idx_target ON reports (target_type, target_id);