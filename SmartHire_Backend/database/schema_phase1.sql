-- =====================================================
-- SmartHire 数据库创建脚本 - 第一阶段
-- 版本: 2.2-phase1
-- 创建日期: 2025-11-07
-- 描述: SmartHire智能招聘系统数据库表结构（前5个核心模块）
-- 说明: 包含用户、简历、职位、投递匹配、沟通5个核心模块，共17张表
-- =====================================================
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smarthire DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smarthire;
-- =====================================================
-- 1. 用户模块 (5张表)
-- =====================================================
-- 1.1 用户基础表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `gender` TINYINT NOT NULL DEFAULT 2 COMMENT '性别：0-女 1-男 2-不愿透露',
    `user_type` TINYINT NOT NULL COMMENT '用户类型：1-求职者 2-HR',
    `status` TINYINT DEFAULT 1 COMMENT '账户状态：0-禁用 1-正常',
    `avatar_url` VARCHAR(255) COMMENT '头像URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_user_type` (`user_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户基础表';
CREATE TABLE `job_seeker` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '求职者ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `birth_date` DATE COMMENT '出生日期',
    `current_city` VARCHAR(50) COMMENT '当前城市',
    `education` TINYINT COMMENT '最高学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士',
    `graduation_year` VARCHAR(10) COMMENT '毕业年份',
    `job_status` TINYINT COMMENT '求职状态 0-离校-尽快到岗 1-在校-尽快到岗 2-在校-考虑机会 3-在校-暂不考虑',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_city` (`current_city`),
    KEY `idx_education` (`education`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '求职者信息表';
-- 1.3 求职期望表
CREATE TABLE `job_seeker_expectation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '求职期望ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `expected_position` VARCHAR(100) NOT NULL COMMENT '期望职位',
    `expected_industry` VARCHAR(100) COMMENT '期望行业',
    `work_city` VARCHAR(50) NOT NULL COMMENT '期望工作城市',
    `salary_min` DECIMAL(10, 2) COMMENT '期望薪资最低',
    `salary_max` DECIMAL(10, 2) COMMENT '期望薪资最高',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`),
    KEY `idx_city_status` (`work_city`),
    KEY `idx_industry` (`expected_industry`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '求职者期望表';
-- 1.4 公司信息表
CREATE TABLE `company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公司ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `company_code` VARCHAR(50) COMMENT '统一社会信用代码',
    `industry` VARCHAR(50) COMMENT '所属行业',
    `company_scale` VARCHAR(20) COMMENT '公司规模',
    `financing_stage` VARCHAR(20) COMMENT '融资阶段',
    `city` VARCHAR(50) COMMENT '所在城市',
    `address` VARCHAR(255) COMMENT '详细地址',
    `website` VARCHAR(255) COMMENT '公司网站',
    `logo_url` VARCHAR(255) COMMENT '公司Logo',
    `description` TEXT COMMENT '公司简介',
    `main_business` TEXT COMMENT '主要业务',
    `benefits` TEXT COMMENT '福利待遇',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-未认证 1-已认证',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_company_code` (`company_code`),
    KEY `idx_company_name` (`company_name`),
    KEY `idx_industry` (`industry`),
    KEY `idx_city` (`city`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公司信息表';
-- 1.5 HR信息表
CREATE TABLE `hr_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'HR ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `position` VARCHAR(50) COMMENT '职位',
    `work_phone` VARCHAR(20) COMMENT '工作电话',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_company_id` (`company_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'HR信息表';
-- =====================================================
-- 2. 简历模块 (6张表)
-- =====================================================
-- 2.1 简历基础表（文件简历）
CREATE TABLE `resume` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '简历ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `resume_name` VARCHAR(100) NOT NULL COMMENT '简历名称',
    `privacy_level` TINYINT DEFAULT 1 COMMENT '隐私级别：1-完全公开 2-仅投递可见',
    `file_url` VARCHAR(255) COMMENT '简历文件URL（PDF/Word等文件）',
    `completeness` INT DEFAULT 0 COMMENT '完整度（0-100）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '简历基础表（用于存储用户上传的文件简历）';
-- 2.2 教育经历表（在线简历）
CREATE TABLE `education_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `school_name` VARCHAR(100) NOT NULL COMMENT '学校名称',
    `major` VARCHAR(100) COMMENT '专业',
    `education` TINYINT COMMENT '学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '教育经历表（在线简历信息）';
-- 2.3 工作经历表（在线简历）
CREATE TABLE `work_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `position` VARCHAR(100) NOT NULL COMMENT '职位',
    `department` VARCHAR(50) COMMENT '部门（可选）',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `is_internship` TINYINT DEFAULT 0 COMMENT '是否实习',
    `description` TEXT COMMENT '工作内容',
    `achievements` TEXT COMMENT '工作成果',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工作经历表（在线简历信息）';
-- 2.4 项目经历表（在线简历）
CREATE TABLE `project_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `project_role` VARCHAR(50) COMMENT '项目角色',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `description` TEXT COMMENT '项目描述',
    `responsibilities` TEXT COMMENT '职责描述',
    `achievements` TEXT COMMENT '项目成果',
    `project_url` VARCHAR(255) COMMENT '项目链接',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '项目经历表（在线简历信息）';
-- 2.5 技能表（在线简历）
CREATE TABLE `skill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '技能ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `skill_name` VARCHAR(50) NOT NULL COMMENT '技能名称',
    `level` TINYINT COMMENT '熟练度 0-了解 1-熟悉 2-掌握',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`),
    KEY `idx_skill_name` (`skill_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '技能表（在线简历信息）';
-- 2.6 证书表（在线简历）
CREATE TABLE `certificate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `certificate_name` VARCHAR(100) NOT NULL COMMENT '证书名称',
    `certificate_url` VARCHAR(255) COMMENT '证书图片URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '证书表（在线简历信息）';
-- =====================================================
-- 3. 职位模块 (2张表)
-- =====================================================
-- 3.1 职位表
CREATE TABLE `job_position` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '职位ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `hr_id` BIGINT NOT NULL COMMENT '发布HR的ID',
    `job_title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `job_category` VARCHAR(50) COMMENT '职位类别',
    `department` VARCHAR(50) COMMENT '部门',
    `city` VARCHAR(50) NOT NULL COMMENT '工作城市',
    `address` VARCHAR(255) COMMENT '详细地址',
    `salary_min` DECIMAL(10, 2) COMMENT '薪资最低',
    `salary_max` DECIMAL(10, 2) COMMENT '薪资最高',
    `salary_months` INT DEFAULT 12 COMMENT '薪资月数',
    `education_required` TINYINT COMMENT '学历要求 0-不限 1-专科 2-本科 3-硕士 4-博士',
    `job_type` TINYINT COMMENT '工作类型 0-全职 1-兼职 2-实习',
    `description` TEXT NOT NULL COMMENT '职位描述',
    `responsibilities` TEXT COMMENT '岗位职责',
    `requirements` TEXT COMMENT '任职要求',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已下线 1-招聘中 2-已暂停',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `application_count` INT DEFAULT 0 COMMENT '申请次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_at` DATETIME COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_hr_id` (`hr_id`),
    KEY `idx_city_status` (`city`, `status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '职位表';
-- 3.2 职位技能要求表
CREATE TABLE `job_skill_requirement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `skill_name` VARCHAR(50) NOT NULL COMMENT '技能名称',
    `is_required` TINYINT DEFAULT 1 COMMENT '是否必需：0-加分项 1-必须',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_skill_name` (`skill_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '职位技能要求表';
-- =====================================================
-- 4. 投递与匹配模块 (2张表)
-- =====================================================
-- 4.1 投递/推荐记录表
CREATE TABLE `application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `resume_id` BIGINT DEFAULT NULL COMMENT '简历ID（投递时必填，推荐时可为空）',
    `initiator` TINYINT NOT NULL COMMENT '发起方：0-求职者投递 1-HR推荐',
    `status` TINYINT DEFAULT 0 COMMENT '状态：
        0-已投递/已推荐
        1-已查看
        2-待面试
        3-已面试
        4-已录用
        5-已拒绝
        6-已撤回',
    `match_score` DECIMAL(5, 2) COMMENT '匹配度分数（0-100）',
    `match_analysis` TEXT COMMENT '匹配分析（JSON格式）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_seeker_job` (`job_id`, `job_seeker_id`),
    KEY `idx_job_seeker_id` (`job_seeker_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '投递/推荐记录表';
-- 4.2 职位收藏表
CREATE TABLE `job_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seeker_job` (`job_seeker_id`, `job_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '职位收藏表';
-- 4.3 候选人收藏表
CREATE TABLE `candidate_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `hr_id` BIGINT NOT NULL COMMENT 'HR ID',
    `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hr_seeker` (`hr_id`, `job_seeker_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '候选人收藏表';
-- =====================================================
-- 5. 沟通模块 (3张表)
-- =====================================================
-- 5.1 聊天消息表
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `application_id` BIGINT NOT NULL COMMENT '投递/推荐记录ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `message_type` TINYINT DEFAULT 1 COMMENT '消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 ',
    `content` TEXT COMMENT '消息内容',
    `file_url` VARCHAR(255) DEFAULT NULL COMMENT '文件/图片/语音/视频URL',
    `reply_to` BIGINT DEFAULT NULL COMMENT '引用的消息ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `is_flagged` TINYINT DEFAULT 0 COMMENT '是否被标记为敏感',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否被逻辑删除/撤回',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_application_created` (`application_id`, `created_at`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '聊天消息表';
-- 5.2 面试安排表
CREATE TABLE `interview` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试ID',
    `application_id` BIGINT NOT NULL COMMENT '投递记录ID',
    `interview_type` TINYINT NOT NULL COMMENT '面试类型：1-电话 2-视频 3-现场',
    `interview_round` INT DEFAULT 1 COMMENT '面试轮次',
    `interview_time` DATETIME NOT NULL COMMENT '面试时间',
    `duration` INT COMMENT '时长（分钟）',
    `location` VARCHAR(255) COMMENT '面试地点',
    `meeting_link` VARCHAR(255) COMMENT '视频链接',
    `interviewer` VARCHAR(100) COMMENT '面试官',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-已完成 3-已取消',
    `feedback` TEXT COMMENT '面试反馈',
    `result` TINYINT COMMENT '面试结果：0-未通过 1-通过',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_interview_time` (`interview_time`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '面试安排表';
-- 5.3 会话表
CREATE TABLE `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` BIGINT NOT NULL COMMENT '用户A',
    `user2_id` BIGINT NOT NULL COMMENT '用户B',
    -- 用于显示最近消息
    `last_message` TEXT COMMENT '最近一条消息内容预览',
    `last_message_time` DATETIME COMMENT '最近消息时间',
    -- 未读数量（每个用户分别统计）
    `unread_count_user1` INT DEFAULT 0,
    `unread_count_user2` INT DEFAULT 0,
    -- 是否固定（置顶）
    `pinned_by_user1` TINYINT DEFAULT 0,
    `pinned_by_user2` TINYINT DEFAULT 0,
    -- 是否删除
    `deleted_by_user1` TINYINT DEFAULT 0,
    `deleted_by_user2` TINYINT DEFAULT 0,
    -- 作为通知功能的字段
    `has_notification_user1` TINYINT DEFAULT 0 COMMENT '用户1是否有未读通知',
    `has_notification_user2` TINYINT DEFAULT 0 COMMENT '用户2是否有未读通知',
    -- 会话创建时间
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_conversation_pair` (`user1_id`, `user2_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '一对一会话';