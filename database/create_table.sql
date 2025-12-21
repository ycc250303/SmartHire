create table application
(
    id             bigint auto_increment comment '记录ID'
        primary key,
    job_id         bigint                             not null comment '职位ID',
    job_seeker_id  bigint                             not null comment '求职者ID',
    resume_id      bigint                             null comment '简历ID（投递时必填，推荐时可为空）',
    initiator      tinyint                            not null comment '发起方：0-求职者投递 1-HR推荐',
    status         tinyint  default 0                 null comment '状态：
        0-已投递/已推荐
        1-已查看
        2-待面试
        3-已面试
        4-已录用
        5-已拒绝
        6-已撤回',
    match_score    decimal(5, 2)                      null comment '匹配度分数（0-100）',
    match_analysis text                               null comment '匹配分析（JSON格式）',
    created_at     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_job_seeker_job
        unique (job_id, job_seeker_id)
)
    comment '投递/推荐记录表' charset = utf8mb4;

create index idx_job_seeker_id
    on application (job_seeker_id);

create index idx_status
    on application (status);

create table ban_record
(
    id                      bigint auto_increment comment '封禁记录ID'
        primary key,
    user_id                 bigint                                not null comment '被封禁的用户ID',
    username                varchar(50)                           not null comment '用户名',
    email                   varchar(100)                          not null comment '用户邮箱',
    user_type               tinyint                               not null comment '用户类型：1-求职者 2-HR',
    ban_reason              text                                  not null comment '封禁原因',
    ban_type                varchar(20)                           not null comment '封禁类型：permanent-永久封禁, temporary-临时封禁',
    ban_days                int                                   null comment '封禁天数（临时封禁时使用）',
    ban_start_time          datetime                              not null comment '封禁开始时间',
    ban_end_time            datetime                              null comment '封禁结束时间（null表示永久封禁）',
    ban_status              varchar(20) default 'active'          not null comment '封禁状态：active-生效中, expired-已过期, lifted-已解除',
    operator_id             bigint                                not null comment '操作管理员ID',
    operator_name           varchar(50)                           not null comment '操作管理员用户名',
    lifted_by_operator_id   bigint                                null comment '解除封禁的管理员ID',
    lifted_by_operator_name varchar(50)                           null comment '解除封禁的管理员用户名',
    lift_reason             text                                  null comment '解除封禁原因',
    lifted_at               datetime                              null comment '解除封禁时间',
    created_at              datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at              datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户封禁记录表' charset = utf8mb4;

create index idx_ban_end_time
    on ban_record (ban_end_time);

create index idx_ban_status
    on ban_record (ban_status);

create index idx_created_at
    on ban_record (created_at);

create index idx_operator_id
    on ban_record (operator_id);

create index idx_operator_time
    on ban_record (operator_id, created_at);

create index idx_user_ban_lookup
    on ban_record (user_id, ban_status, ban_end_time);

create index idx_user_id
    on ban_record (user_id);

create table candidate_favorite
(
    id            bigint auto_increment comment '收藏ID'
        primary key,
    hr_id         bigint                             not null comment 'HR ID',
    job_seeker_id bigint                             not null comment '求职者ID',
    created_at    datetime default CURRENT_TIMESTAMP null comment '收藏时间',
    constraint uk_hr_seeker
        unique (hr_id, job_seeker_id)
)
    comment '候选人收藏表' charset = utf8mb4;

create table certificate
(
    id               bigint auto_increment comment '证书ID'
        primary key,
    job_seeker_id    bigint                             not null comment '求职者ID',
    certificate_name varchar(100)                       not null comment '证书名称',
    certificate_url  varchar(255)                       null comment '证书图片URL',
    created_at       datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '证书表（在线简历信息）' charset = utf8mb4;

create index idx_job_seeker_id
    on certificate (job_seeker_id);

create table chat_message
(
    id              bigint auto_increment comment '消息ID'
        primary key,
    application_id  bigint                             not null comment '投递/推荐记录ID',
    sender_id       bigint                             not null comment '发送者用户ID',
    receiver_id     bigint                             not null comment '接收者用户ID',
    message_type    tinyint  default 1                 null comment '消息类型：1-文本 2-图片 3-文件 4-语音 5-视频 6-系统通知 7-卡片消息',
    content         text                               null comment '消息内容',
    file_url        varchar(255)                       null comment '文件/图片/语音/视频URL',
    reply_to        bigint                             null comment '引用的消息ID',
    is_read         tinyint  default 0                 null comment '是否已读',
    is_flagged      tinyint  default 0                 null comment '是否被标记为敏感',
    is_deleted      tinyint  default 0                 null comment '是否被逻辑删除/撤回',
    created_at      datetime default CURRENT_TIMESTAMP null comment '发送时间',
    conversation_id bigint                             not null comment '会话ID'
)
    comment '聊天消息表' charset = utf8mb4;

create index idx_application_created
    on chat_message (application_id, created_at);

create index idx_application_id
    on chat_message (application_id);

create index idx_conversation
    on chat_message (conversation_id);

create index idx_created_at
    on chat_message (created_at);

create index idx_sender_receiver
    on chat_message (sender_id, receiver_id);

create table company
(
    id                 bigint auto_increment comment '公司ID'
        primary key,
    company_name       varchar(100)                       not null comment '公司名称',
    industry           varchar(50)                        null comment '所属行业',
    company_scale      tinyint                            null comment '公司规模 1:0-20 2：20-99 3:100-499 4:500-999 5:1000-3000 6:3000-10000 7:10000以上',
    financing_stage    tinyint                            null comment '融资阶段 0:无融资 1:天使轮 2:A轮 3:B轮 4:C轮 5:D轮',
    website            varchar(255)                       null comment '公司网站',
    logo_url           varchar(255)                       null comment '公司Logo',
    description        text                               null comment '公司简介',
    main_business      text                               null comment '主要业务',
    benefits           text                               null comment '福利待遇',
    status             tinyint  default 1                 null comment '状态：0-未认证 1-已认证',
    created_at         datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at         datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    company_created_at datetime                           not null comment '公司创建时间',
    registered_capital varchar(20)                        not null comment '注册资本'
)
    comment '公司信息表' charset = utf8mb4;

create index idx_company_name
    on company (company_name);

create index idx_industry
    on company (industry);

create table conversation
(
    id                     bigint auto_increment comment '会话ID'
        primary key,
    user1_id               bigint                             not null comment '用户A',
    user2_id               bigint                             not null comment '用户B',
    last_message           text                               null comment '最近一条消息内容预览',
    last_message_time      datetime                           null comment '最近消息时间',
    unread_count_user1     int      default 0                 null,
    unread_count_user2     int      default 0                 null,
    pinned_by_user1        tinyint  default 0                 null,
    pinned_by_user2        tinyint  default 0                 null,
    has_notification_user1 tinyint  default 0                 null comment '用户1是否有未读通知',
    has_notification_user2 tinyint  default 0                 null comment '用户2是否有未读通知',
    created_at             datetime default CURRENT_TIMESTAMP null,
    deleted_by_user1       tinyint  default 0                 null,
    deleted_by_user2       tinyint  default 0                 null,
    constraint uniq_conversation_pair
        unique (user1_id, user2_id)
)
    comment '一对一会话' charset = utf8mb4;

create table education_experience
(
    id            bigint auto_increment comment '记录ID'
        primary key,
    job_seeker_id bigint                             not null comment '求职者ID',
    school_name   varchar(100)                       not null comment '学校名称',
    major         varchar(100)                       null comment '专业',
    education     tinyint                            null comment '学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士',
    start_year    date                               not null comment '开始年份',
    end_year      date                               null comment '结束年份',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '教育经历表（在线简历信息）' charset = utf8mb4;

create index idx_job_seeker_id
    on education_experience (job_seeker_id);

create table hr_audit_record
(
    id            bigint auto_increment comment '审核记录ID'
        primary key,
    hr_id         bigint                             not null comment '被审核HR ID',
    user_id       bigint                             not null comment '被审核HR的用户ID',
    company_id    bigint                             not null comment '公司ID',
    hr_name       varchar(50)                        not null comment 'HR姓名',
    audit_status  varchar(20)                        not null comment '审核状态: pending-待审核 approved-已通过 rejected-已拒绝',
    auditor_id    bigint                             null comment '审核员ID（公司管理员）',
    auditor_name  varchar(50)                        null comment '审核员姓名',
    audit_reason  text                               null comment '审核意见',
    reject_reason text                               null comment '拒绝原因',
    audited_at    datetime                           null comment '审核时间',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'HR审核记录表' charset = utf8mb4;

create index idx_audit_status
    on hr_audit_record (audit_status);

create index idx_auditor_id
    on hr_audit_record (auditor_id);

create index idx_company_id
    on hr_audit_record (company_id);

create index idx_hr_id
    on hr_audit_record (hr_id);

create index idx_user_id
    on hr_audit_record (user_id);

create table hr_info
(
    id               bigint auto_increment comment 'HR ID'
        primary key,
    user_id          bigint                             not null comment '用户ID',
    company_id       bigint                             not null comment '公司ID',
    real_name        varchar(50)                        not null comment '真实姓名',
    position         varchar(50)                        null comment '职位',
    work_phone       varchar(20)                        null comment '工作电话',
    is_company_admin tinyint  default 0                 null comment '是否为公司管理员：0-否 1-是',
    created_at       datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_id
        unique (user_id)
)
    comment 'HR信息表' charset = utf8mb4;

create index idx_company_admin
    on hr_info (company_id, is_company_admin);

create index idx_company_id
    on hr_info (company_id);

create table interview
(
    id              bigint auto_increment comment '面试ID'
        primary key,
    application_id  bigint                             not null comment '投递记录ID',
    interview_type  tinyint                            not null comment '面试类型：1-电话 2-视频 3-现场',
    interview_round int      default 1                 null comment '面试轮次',
    interview_time  datetime                           not null comment '面试时间',
    duration        int                                null comment '时长（分钟）',
    location        varchar(255)                       null comment '面试地点',
    meeting_link    varchar(255)                       null comment '视频链接',
    interviewer     varchar(100)                       null comment '面试官',
    status          tinyint  default 0                 null comment '状态：0-待确认 1-已确认 2-已完成 3-已取消',
    feedback        text                               null comment '面试反馈',
    result          tinyint                            null comment '面试结果：0-未通过 1-通过',
    created_at      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '面试安排表' charset = utf8mb4;

create index idx_application_id
    on interview (application_id);

create index idx_interview_time
    on interview (interview_time);

create index idx_status
    on interview (status);

create table job_audit_record
(
    id                   bigint auto_increment comment '审核记录ID'
        primary key,
    job_id               bigint                             not null comment '职位ID',
    job_title            varchar(100)                       not null comment '职位名称',
    company_id           bigint                             not null comment '公司ID',
    hr_id                bigint                             not null comment 'HR ID',
    company_name         varchar(100)                       not null comment '公司名称',
    hr_name              varchar(50)                        not null comment 'HR姓名',
    audit_note           text                               null comment '审核备注',
    audit_reason         text                               null comment '审核意见（要求修改时使用）',
    reject_reason        text                               null comment '拒绝原因',
    audit_status         varchar(20)                        not null comment '审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    company_audit_status varchar(20)                        null comment '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    company_auditor_id   bigint                             null comment '公司审核员ID',
    company_auditor_name varchar(50)                        null comment '公司审核员姓名',
    company_audited_at   datetime                           null comment '公司审核时间',
    system_audit_status  varchar(20)                        null comment '系统审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    system_auditor_id    bigint                             null comment '系统审核员ID',
    system_auditor_name  varchar(50)                        null comment '系统审核员姓名',
    system_audited_at    datetime                           null comment '系统审核时间',
    auditor_id           bigint                             null comment '审核员ID',
    auditor_name         varchar(50)                        null comment '审核员姓名',
    audited_at           datetime                           null comment '审核时间',
    created_at           datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '职位审核记录表' charset = utf8mb4;

create index idx_audit_status
    on job_audit_record (audit_status);

create index idx_auditor_id
    on job_audit_record (auditor_id);

create index idx_company_audit_status
    on job_audit_record (company_audit_status);

create index idx_company_auditor_id
    on job_audit_record (company_auditor_id);

create index idx_created_at
    on job_audit_record (created_at);

create index idx_job_id
    on job_audit_record (job_id);

create index idx_system_audit_status
    on job_audit_record (system_audit_status);

create table job_favorite
(
    id            bigint auto_increment comment '收藏ID'
        primary key,
    job_seeker_id bigint                             not null comment '求职者ID',
    job_id        bigint                             not null comment '职位ID',
    created_at    datetime default CURRENT_TIMESTAMP null comment '收藏时间',
    constraint uk_seeker_job
        unique (job_seeker_id, job_id)
)
    comment '职位收藏表' charset = utf8mb4;

create table job_filter
(
    id                       bigint auto_increment comment '筛选ID'
        primary key,
    job_seeker_id            bigint                             not null comment '求职者ID',
    job_type                 tinyint                            not null comment '岗位类型：0-全职 1-实习',
    education_required       tinyint  default 0                 null comment '学历要求：0-不限 1-高中及以下 2-专科 3-本科 4-硕士 5-博士',
    company_scale            tinyint                            null comment '公司规模：NULL-不限 1:0-20 2:20-99 3:100-499 4:500-999 5:1000-3000 6:3000-10000 7:10000以上',
    financing_stage          tinyint                            null comment '融资阶段：NULL-不限 0:无融资 1:天使轮 2:A轮 3:B轮 4:C轮 5:D轮 6:已上市',
    internship_duration      tinyint  default 0                 null comment '实习时长（仅实习类型）：0-不限 1-1个月 2-1-3个月 3-3-6个月 4-6个月以上',
    internship_days_per_week tinyint  default 0                 null comment '每周出勤天数（仅实习类型）：0-不限 1-1天 2-2天 3-3天 4-4天 5-5天',
    internship_salary_range  tinyint  default 0                 null comment '实习日薪范围（仅实习类型）：0-不限 1-100以下 2-100-200 3-200-300 4-300-400 5-400-500 6-500以上',
    fulltime_salary_range    tinyint  default 0                 null comment '全职月薪范围（仅全职类型）：0-不限 1-3k以下 2-3-5k 3-5-10k 4-10-15k 5-15-20k 6-20-30k 7-30-50k 8-50k以上',
    experience_required      tinyint  default 0                 null comment '经验要求（仅全职类型）：0-不限 1-应届生 2-1年以内 3-1-3年 4-3-5年 5-5-10年 6-10年以上',
    created_at               datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at               datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '岗位筛选表' charset = utf8mb4;

create index idx_job_seeker_id
    on job_filter (job_seeker_id);

create index idx_job_type
    on job_filter (job_type);

create table job_info
(
    id                         bigint auto_increment comment '职位ID'
        primary key,
    company_id                 bigint                                not null comment '公司ID',
    hr_id                      bigint                                not null comment '发布HR的ID',
    job_title                  varchar(100)                          not null comment '职位名称',
    job_category               varchar(50)                           null comment '职位类别',
    department                 varchar(50)                           null comment '部门',
    city                       varchar(50)                           not null comment '工作城市',
    address                    varchar(255)                          null comment '详细地址',
    salary_min                 decimal(10, 2)                        null comment '薪资最低',
    salary_max                 decimal(10, 2)                        null comment '薪资最高',
    salary_months              int         default 12                null comment '薪资月数',
    education_required         tinyint                               null comment '学历要求 0-不限 1-专科 2-本科 3-硕士 4-博士',
    job_type                   tinyint                               null comment '工作类型 0-全职 1-实习',
    description                text                                  not null comment '职位描述',
    responsibilities           text                                  null comment '岗位职责',
    requirements               text                                  null comment '任职要求',
    status                     tinyint     default 1                 null comment '状态：0-已下线 1-招聘中 2-已暂停',
    view_count                 int         default 0                 null comment '浏览次数',
    application_count          int         default 0                 null comment '申请次数',
    created_at                 datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at                 datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    published_at               datetime                              null comment '发布时间',
    internship_days_per_week   int                                   null comment '每周实习天数（仅实习类型需要）',
    internship_duration_months int                                   null comment '实习时长（月为单位，仅实习类型需要）',
    experience_required        tinyint                               null comment '经验要求（仅全职类型需要）0-应届生 1-1年以内 2-1-3年 3-3-5年 4-5-10年 5-10年以上',
    audit_status               varchar(20) default 'draft'           null comment '审核状态: draft-草稿 pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    company_audit_status       varchar(20)                           null comment '公司审核状态: pending-待审核 approved-已通过 rejected-已拒绝 modified-需修改',
    submitted_at               datetime                              null comment '提交审核时间',
    audited_at                 datetime                              null comment '审核完成时间'
)
    comment '职位表' charset = utf8mb4;

create index idx_audit_status
    on job_info (audit_status);

create index idx_audited_at
    on job_info (audited_at);

create index idx_city_status
    on job_info (city, status);

create index idx_company_audit_status
    on job_info (company_audit_status);

create index idx_company_id
    on job_info (company_id);

create index idx_hr_id
    on job_info (hr_id);

create index idx_published_at
    on job_info (published_at);

create index idx_submitted_at
    on job_info (submitted_at);

create table job_seeker
(
    id                    bigint auto_increment comment '求职者ID'
        primary key,
    user_id               bigint                             not null comment '用户ID',
    real_name             varchar(50)                        not null comment '真实姓名',
    birth_date            date                               null comment '出生日期',
    current_city          varchar(50)                        null comment '当前城市',
    education             tinyint                            null comment '最高学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士''',
    job_status            tinyint                            null comment '求职状态 0-离校-尽快到岗 1-在校-尽快到岗 2-在校-考虑机会 3-在校-暂不考虑',
    created_at            datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at            datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    graduation_year       varchar(10)                        null comment '毕业年份',
    work_experience_year  int      default 0                 not null comment '工作经验年份',
    internship_experience tinyint  default 0                 not null comment '是否有实习经历',
    constraint uk_user_id
        unique (user_id)
)
    comment '求职者信息表' charset = utf8mb4;

create index idx_city
    on job_seeker (current_city);

create index idx_education
    on job_seeker (education);

create table job_seeker_expectation
(
    id                bigint auto_increment comment '求职期望ID'
        primary key,
    job_seeker_id     bigint                             not null comment '求职者ID',
    expected_position varchar(100)                       not null comment '期望职位',
    expected_industry varchar(100)                       null comment '期望行业',
    work_city         varchar(50)                        not null comment '期望工作城市',
    salary_min        decimal(10, 2)                     null comment '期望薪资最低',
    salary_max        decimal(10, 2)                     null comment '期望薪资最高',
    created_at        datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '求职者期望表' charset = utf8mb4;

create index idx_city_status
    on job_seeker_expectation (work_city);

create index idx_industry
    on job_seeker_expectation (expected_industry);

create index idx_job_seeker_id
    on job_seeker_expectation (job_seeker_id);

create index idx_salary_max
    on job_seeker_expectation (salary_max);

create index idx_salary_min
    on job_seeker_expectation (salary_min);

create table job_skill_requirement
(
    id          bigint auto_increment comment '记录ID'
        primary key,
    job_id      bigint                             not null comment '职位ID',
    skill_name  varchar(50)                        not null comment '技能名称',
    is_required tinyint  default 1                 null comment '是否必需：0-加分项 1-必须',
    created_at  datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '职位技能要求表' charset = utf8mb4;

create index idx_job_id
    on job_skill_requirement (job_id);

create index idx_skill_name
    on job_skill_requirement (skill_name);

create table project_experience
(
    id             bigint auto_increment comment '记录ID'
        primary key,
    job_seeker_id  bigint                             not null comment '求职者ID',
    project_name   varchar(100)                       not null comment '项目名称',
    project_role   varchar(50)                        null comment '项目角色',
    start_date     date                               not null comment '开始日期',
    end_date       date                               null comment '结束日期',
    description    text                               null comment '项目描述',
    responsibility text                               null comment '职责描述',
    achievement    text                               null comment '项目成果',
    project_url    varchar(255)                       null comment '项目链接',
    created_at     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '项目经历表（在线简历信息）' charset = utf8mb4;

create index idx_job_seeker_id
    on project_experience (job_seeker_id);

create table reports
(
    id             bigint auto_increment comment '举报ID'
        primary key,
    reporter_id    bigint                             not null comment '举报人ID（关联user表）',
    target_type    tinyint                            not null comment '举报对象类型：1-用户, 2-职位',
    target_id      bigint                             not null comment '举报对象ID',
    report_type    tinyint                            not null comment '举报类型：1-垃圾信息, 2-不当内容, 3-虚假职位, 4-欺诈行为, 5-骚扰行为, 6-其他',
    reason         text                               not null comment '举报原因/详细描述',
    status         tinyint  default 0                 not null comment '处理状态：0-待处理, 1-已处理',
    handler_id     bigint                             null comment '处理人ID（管理员ID）',
    handle_result  tinyint                            null comment '处理结果：0-无需处理, 1-警告, 2-封禁/下线',
    handle_reason  text                               null comment '处理原因',
    handle_time    datetime                           null comment '处理时间',
    evidence_image longtext                           null comment '举报证据图片（base64编码，支持jpg/pdf）',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '举报信息主表' collate = utf8mb4_unicode_ci;

create index idx_created_at
    on reports (created_at);

create index idx_handler_id
    on reports (handler_id);

create index idx_report_type
    on reports (report_type);

create index idx_reporter_id
    on reports (reporter_id);

create index idx_status
    on reports (status);

create index idx_target
    on reports (target_type, target_id);

create table resume
(
    id            bigint auto_increment comment '简历ID'
        primary key,
    job_seeker_id bigint                             not null comment '求职者ID',
    resume_name   varchar(100)                       not null comment '简历名称',
    privacy_level tinyint  default 1                 null comment '隐私级别：1-公开 2-仅投递可见',
    file_url      varchar(255)                       null comment '简历文件URL',
    completeness  int      default 0                 null comment '完整度（0-100）(AI评估）',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '简历基础表' charset = utf8mb4;

create index idx_job_seeker_id
    on resume (job_seeker_id);

create table skill
(
    id            bigint auto_increment comment '技能ID'
        primary key,
    job_seeker_id bigint                             not null comment '求职者ID',
    skill_name    varchar(50)                        not null comment '技能名称',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    level         tinyint                            null comment '技能熟练度 0-了解 1-熟悉 2-掌握',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '技能表（在线简历信息）' charset = utf8mb4;

create index idx_job_seeker_id
    on skill (job_seeker_id);

create index idx_skill_name
    on skill (skill_name);

create table user
(
    id            bigint auto_increment comment '用户ID'
        primary key,
    username      varchar(50)                        not null comment '用户名',
    password      varchar(255)                       not null comment '加密密码',
    email         varchar(100)                       not null comment '邮箱',
    phone         varchar(20)                        null comment '手机号',
    user_type     tinyint                            not null comment '用户类型：1-求职者 2-HR',
    status        tinyint  default 1                 null comment '账户状态：0-禁用 1-正常',
    avatar_url    varchar(255)                       null comment '头像URL',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    last_login_at datetime                           null comment '最后登录时间',
    gender        tinyint  default 2                 not null comment '性别 0-男 1-女 2-不愿透露',
    constraint uk_email
        unique (email),
    constraint uk_phone
        unique (phone),
    constraint uk_username
        unique (username)
)
    comment '用户基础表' charset = utf8mb4;

create index idx_user_type
    on user (user_type);

create table work_experience
(
    id            bigint auto_increment comment '记录ID'
        primary key,
    job_seeker_id bigint                             not null comment '求职者ID',
    company_name  varchar(100)                       not null comment '公司名称',
    position      varchar(100)                       not null comment '职位',
    department    varchar(50)                        null comment '部门（可选）',
    start_date    date                               not null comment '开始日期',
    end_date      date                               null comment '结束日期',
    is_internship tinyint  default 0                 null comment '是否实习 0-不是 1-是',
    description   text                               null comment '工作内容',
    achievements  text                               null comment '工作成果',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '工作经历表（在线简历信息）' charset = utf8mb4;

create index idx_job_seeker_id
    on work_experience (job_seeker_id);

