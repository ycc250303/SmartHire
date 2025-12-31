package com.SmartHire.common.dto.hrDto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import java.util.List;

/** 职位信息DTO */
@Data
public class JobInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 职位ID */
    private Long id;

    /** 公司ID */
    private Long companyId;

    /** 发布HR的ID */
    private Long hrId;

    /** 职位名称 */
    private String jobTitle;

    /** 职位类别 */
    private String jobCategory;

    /** 部门 */
    private String department;

    /** 工作城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 薪资最低 */
    private BigDecimal salaryMin;

    /** 薪资最高 */
    private BigDecimal salaryMax;

    /** 薪资月数 */
    private Integer salaryMonths;

    /** 学历要求 */
    private Integer educationRequired;

    /** 工作类型 */
    private Integer jobType;

    /** 经验要求 */
    private Integer experienceRequired;

    /** 每周实习天数 */
    private Integer internshipDaysPerWeek;

    /** 实习时长 */
    private Integer internshipDurationMonths;

    /** 职位描述 */
    private String description;

    /** 岗位职责 */
    private String responsibilities;

    /** 任职要求 */
    private String requirements;

    /** 状态：0-已下线 1-招聘中 2-已暂停 */
    private Integer status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 申请次数 */
    private Integer applicationCount;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 发布时间 */
    private Date publishedAt;

    /** 审核状态 */
    private String auditStatus;

    /** 公司审核状态 */
    private String companyAuditStatus;

    /** 提交审核时间 */
    private Date submittedAt;

    /** 审核完成时间 */
    private Date auditedAt;

    /** 技能要求列表 */
    private List<String> skills;
}
