# SmartHire 数据库表结构文档

数据库连接信息：
- **服务器**: 111.229.81.45
- **数据库名**: SmartHire
- **用户名**: SmartHireManager
- **密码**: SmartHire123

## 数据库概览

数据库中共有 **19个表**，涵盖了智能招聘系统的完整功能模块，包括用户管理、公司信息、职位管理、求职申请、聊天通讯、面试安排等核心业务功能。

---

## 表结构详情

### 1. application（求职申请表）
存储求职者对职位的申请信息及匹配分析结果。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_id | bigint | NO | MUL | - | 职位ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| resume_id | bigint | YES | | - | 简历ID |
| initiator | tinyint | NO | | - | 发起方（1-HR/0-求职者） |
| status | tinyint | YES | MUL | 0 | 申请状态 |
| match_score | decimal(5,2) | YES | | - | 匹配分数 |
| match_analysis | text | YES | | - | 匹配分析 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 2. candidate_favorite（候选人收藏表）
HR收藏的候选人信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| hr_id | bigint | NO | MUL | - | HR用户ID |
| job_seeker_id | bigint | NO | | - | 求职者ID |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 收藏时间 |

### 3. certificate（证书表）
求职者的证书信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| certificate_name | varchar(100) | NO | | - | 证书名称 |
| certificate_url | varchar(255) | YES | | - | 证书文件链接 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |

### 4. chat_message（聊天消息表）
HR与求职者之间的聊天消息记录。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| application_id | bigint | NO | MUL | - | 申请ID |
| sender_id | bigint | NO | MUL | - | 发送者ID |
| receiver_id | bigint | NO | | - | 接收者ID |
| message_type | tinyint | YES | | 1 | 消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息 8-面试邀请 9-Offer 10-拒绝通知 |
| content | text | YES | | - | 消息内容 |
| file_url | varchar(255) | YES | | - | 文件链接 |
| reply_to | bigint | YES | | - | 回复的消息ID |
| is_read | tinyint | YES | | 0 | 是否已读 |
| is_flagged | tinyint | YES | | 0 | 是否标记 |
| is_deleted | tinyint | YES | | 0 | 是否删除 |
| created_at | datetime | YES | MUL | CURRENT_TIMESTAMP | 创建时间 |
| conversation_id | bigint | NO | MUL | - | 会话ID |

### 5. company（公司表）
企业基本信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| company_name | varchar(100) | NO | MUL | - | 公司名称 |
| industry | varchar(50) | YES | MUL | - | 所属行业 |
| company_scale | tinyint | YES | | - | 公司规模 |
| financing_stage | tinyint | YES | | - | 融资阶段 |
| website | varchar(255) | YES | | - | 官方网站 |
| logo_url | varchar(255) | YES | | - | 公司Logo |
| description | text | YES | | - | 公司描述 |
| main_business | text | YES | | - | 主营业务 |
| benefits | text | YES | | - | 福利待遇 |
| status | tinyint | YES | | 1 | 状态 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| company_created_at | datetime | NO | | - | 公司成立时间 |
| registered_capital | varchar(20) | NO | | - | 注册资本 |

### 6. conversation（会话表）
用户间的会话信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| user1_id | bigint | NO | MUL | - | 用户1 ID |
| user2_id | bigint | NO | | - | 用户2 ID |
| last_message | text | YES | | - | 最后一条消息 |
| last_message_time | datetime | YES | | - | 最后消息时间 |
| unread_count_user1 | int | YES | | 0 | 用户1未读数 |
| unread_count_user2 | int | YES | | 0 | 用户2未读数 |
| pinned_by_user1 | tinyint | YES | | 0 | 用户1是否置顶 |
| pinned_by_user2 | tinyint | YES | | 0 | 用户2是否置顶 |
| has_notification_user1 | tinyint | YES | | 0 | 用户1是否有通知 |
| has_notification_user2 | tinyint | YES | | 0 | 用户2是否有通知 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| deleted_by_user1 | tinyint | YES | | 0 | 用户1是否删除 |
| deleted_by_user2 | tinyint | YES | | 0 | 用户2是否删除 |

### 7. education_experience（教育经历表）
求职者的教育背景信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| school_name | varchar(100) | NO | | - | 学校名称 |
| major | varchar(100) | YES | | - | 专业 |
| education | tinyint | YES | | - | 学历层次 |
| start_year | date | NO | | - | 开始时间 |
| end_year | date | YES | | - | 结束时间 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 8. hr_info（HR信息表）
HR用户的详细信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| user_id | bigint | NO | UNI | - | 关联用户ID |
| company_id | bigint | NO | MUL | - | 所属公司ID |
| real_name | varchar(50) | NO | | - | 真实姓名 |
| position | varchar(50) | YES | | - | 职位 |
| work_phone | varchar(20) | YES | | - | 工作电话 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 9. interview（面试表）
面试安排信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| application_id | bigint | NO | MUL | - | 申请ID |
| interview_type | tinyint | NO | | - | 面试类型 |
| interview_round | int | YES | | 1 | 面试轮次 |
| interview_time | datetime | NO | MUL | - | 面试时间 |
| duration | int | YES | | - | 持续时间（分钟） |
| location | varchar(255) | YES | | - | 面试地点 |
| meeting_link | varchar(255) | YES | | - | 在线会议链接 |
| interviewer | varchar(100) | YES | | - | 面试官 |
| status | tinyint | YES | MUL | 0 | 面试状态 |
| feedback | text | YES | | - | 面试反馈 |
| result | tinyint | YES | | - | 面试结果 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 10. job_favorite（职位收藏表）
求职者收藏的职位信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| job_id | bigint | NO | | - | 职位ID |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 收藏时间 |

### 11. job_info（职位信息表）
企业发布的职位信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| company_id | bigint | NO | MUL | - | 公司ID |
| hr_id | bigint | NO | MUL | - | 发布HR的ID |
| job_title | varchar(100) | NO | | - | 职位标题 |
| job_category | varchar(50) | YES | | - | 职位类别 |
| department | varchar(50) | YES | | - | 所属部门 |
| city | varchar(50) | NO | MUL | - | 工作城市 |
| address | varchar(255) | YES | | - | 详细地址 |
| salary_min | decimal(10,2) | YES | | - | 最低薪资 |
| salary_max | decimal(10,2) | YES | | - | 最高薪资 |
| salary_months | int | YES | | 12 | 薪资月数 |
| education_required | tinyint | YES | | - | 学历要求 |
| job_type | tinyint | YES | | - | 工作类型 |
| description | text | NO | | - | 职位描述 |
| responsibilities | text | YES | | - | 工作职责 |
| requirements | text | YES | | - | 任职要求 |
| status | tinyint | YES | | 1 | 职位状态 |
| view_count | int | YES | | 0 | 浏览次数 |
| application_count | int | YES | | 0 | 申请次数 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| published_at | datetime | YES | MUL | - | 发布时间 |
| internship_days_per_week | int | YES | | - | 实习天数/周 |
| internship_duration_months | int | YES | | - | 实习持续月数 |
| experience_required | tinyint | YES | | - | 经验要求 |

### 12. job_seeker（求职者表）
求职者的基本信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| user_id | bigint | NO | UNI | - | 关联用户ID |
| real_name | varchar(50) | NO | | - | 真实姓名 |
| birth_date | date | YES | | - | 出生日期 |
| current_city | varchar(50) | YES | MUL | - | 当前所在城市 |
| education | tinyint | YES | MUL | - | 最高学历 |
| job_status | tinyint | YES | | - | 求职状态 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| graduation_year | varchar(10) | YES | | - | 毕业年份 |

### 13. job_seeker_expectation（求职期望表）
求职者的求职期望信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| expected_position | varchar(100) | NO | | - | 期望职位 |
| expected_industry | varchar(100) | YES | MUL | - | 期望行业 |
| work_city | varchar(50) | NO | MUL | - | 期望工作城市 |
| salary_min | decimal(10,2) | YES | MUL | - | 最低期望薪资 |
| salary_max | decimal(10,2) | YES | MUL | - | 最高期望薪资 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 14. job_skill_requirement（职位技能要求表）
职位对技能的要求。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_id | bigint | NO | MUL | - | 职位ID |
| skill_name | varchar(50) | NO | MUL | - | 技能名称 |
| is_required | tinyint | YES | | 1 | 是否必需 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |

### 15. project_experience（项目经历表）
求职者的项目经历信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| project_name | varchar(100) | NO | | - | 项目名称 |
| project_role | varchar(50) | YES | | - | 项目角色 |
| start_date | date | NO | | - | 开始日期 |
| end_date | date | YES | | - | 结束日期 |
| description | text | YES | | - | 项目描述 |
| responsibility | text | YES | | - | 项目职责 |
| achievement | text | YES | | - | 项目成就 |
| project_url | varchar(255) | YES | | - | 项目链接 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 16. resume（简历表）
求职者的简历信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| resume_name | varchar(100) | NO | | - | 简历名称 |
| privacy_level | tinyint | YES | | 1 | 隐私级别 |
| file_url | varchar(255) | YES | | - | 简历文件链接 |
| completeness | int | YES | | 0 | 完整度 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 17. skill（技能表）
求职者拥有的技能信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| skill_name | varchar(50) | NO | MUL | - | 技能名称 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| level | tinyint | YES | | - | 技能水平 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

### 18. user（用户表）
系统用户基本信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| username | varchar(50) | NO | UNI | - | 用户名 |
| password | varchar(255) | NO | | - | 密码（加密存储） |
| email | varchar(100) | NO | UNI | - | 邮箱 |
| phone | varchar(20) | YES | UNI | - | 手机号 |
| user_type | tinyint | NO | MUL | - | 用户类型 |
| status | tinyint | YES | | 1 | 账户状态 |
| avatar_url | varchar(255) | YES | | - | 头像链接 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |
| last_login_at | datetime | YES | | - | 最后登录时间 |
| gender | tinyint | NO | | 2 | 性别 |

### 19. work_experience（工作经历表）
求职者的工作经历信息。

| 字段名 | 类型 | 是否为空 | 键 | 默认值 | 说明 |
|--------|------|----------|-----|--------|------|
| id | bigint | NO | PRI | auto_increment | 主键ID |
| job_seeker_id | bigint | NO | MUL | - | 求职者ID |
| company_name | varchar(100) | NO | | - | 公司名称 |
| position | varchar(100) | NO | | - | 职位名称 |
| department | varchar(50) | YES | | - | 部门 |
| start_date | date | NO | | - | 开始日期 |
| end_date | date | YES | | - | 结束日期 |
| is_internship | tinyint | YES | | 0 | 是否实习 |
| description | text | YES | | - | 工作描述 |
| achievements | text | YES | | - | 工作成就 |
| created_at | datetime | YES | | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | YES | | CURRENT_TIMESTAMP | 更新时间 |

---

## 数据库关系说明

### 核心实体关系
1. **用户中心化设计**
   - `user` 表为核心，通过 `user_type` 区分不同角色
   - `job_seeker` 和 `hr_info` 分别扩展不同角色的详细信息

2. **职位申请流程**
   - `company` 发布职位到 `job_info`
   - `job_seeker` 通过 `application` 表申请职位
   - 申请流程中包含 `interview` 面试安排

3. **实时通讯**
   - `conversation` 表维护会话关系
   - `chat_message` 表存储具体聊天内容

4. **个人能力展示**
   - `education_experience` 教育经历
   - `work_experience` 工作经历
   - `project_experience` 项目经历
   - `skill` 技能证书
   - `certificate` 资质证书

5. **收藏功能**
   - `job_favorite` 求职者收藏职位
   - `candidate_favorite` HR收藏候选人

### 字段说明

#### 枚举类型字段
- **user_type**: 用户类型（0-求职者, 1-HR, 2-管理员）
- **status**: 状态（0-禁用, 1-启用）
- **gender**: 性别（0-女, 1-男, 2-保密）
- **education**: 学历（0-高中及以下, 1-大专, 2-本科, 3-硕士, 4-博士）
- **job_type**: 工作类型（0-全职, 1-兼职, 2-实习）
- **interview_type**: 面试类型（0-现场面试, 1-视频面试, 2-电话面试）

---

## 生成时间
文档生成时间：2025-12-06