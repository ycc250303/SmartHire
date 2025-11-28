package com.SmartHire.hrService.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 岗位列表DTO
 */
@Data
public class JobPositionListDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职位ID
     */
    private Long id;

    /**
     * 职位名称
     */
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
    private String city;

    /**
     * 薪资最低
     */
    private BigDecimal salaryMin;

    /**
     * 薪资最高
     */
    private BigDecimal salaryMax;

    /**
     * 薪资月数
     */
    private Integer salaryMonths;

    /**
     * 学历要求
     */
    private Integer educationRequired;

    /**
     * 工作类型
     */
    private Integer jobType;

    /**
     * 状态：0-已下线 1-招聘中 2-已暂停
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 申请次数
     */
    private Integer applicationCount;

    /**
     * 发布时间
     */
    private Date publishedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 技能要求列表
     */
    private List<String> skills;
}
