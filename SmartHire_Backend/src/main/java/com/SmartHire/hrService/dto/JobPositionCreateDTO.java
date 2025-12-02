package com.SmartHire.hrService.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 岗位创建DTO
 */
@Data
public class JobPositionCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公司ID
     */
    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空")
    private String jobTitle;

    /**
     * 职位类别
     */
    private String jobCategory;

    /**
     * 部门
     */
    private String department;

    /**
     * 工作城市
     */
    @NotBlank(message = "工作城市不能为空")
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 薪资最低
     */
    @Min(value = 0, message = "最低薪资不能小于0")
    private BigDecimal salaryMin;

    /**
     * 薪资最高
     */
    @Min(value = 0, message = "最高薪资不能小于0")
    private BigDecimal salaryMax;

    /**
     * 薪资月数
     */
    @Min(value = 1, message = "薪资月数不能小于1")
    @Max(value = 24, message = "薪资月数不能大于24")
    private Integer salaryMonths;

    /**
     * 学历要求：0-不限 1-专科 2-本科 3-硕士 4-博士
     */
    private Integer educationRequired;

    /**
     * 工作类型：0-全职 1-兼职 2-实习
     */
    @NotNull(message = "工作类型不能为空")
    private Integer jobType;

    /**
     * 职位描述
     */
    @NotBlank(message = "职位描述不能为空")
    private String description;

    /**
     * 岗位职责
     */
    private String responsibilities;

    /**
     * 任职要求
     */
    private String requirements;

    /**
     * 状态：0-已下线 1-招聘中 2-已暂停（默认为1-招聘中）
     */
    private Integer status;

    /**
     * 技能要求列表
     */
    private List<JobSkillDTO> skills;
}

