package com.SmartHire.hrService.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 投递列表DTO
 */
@Data
public class ApplicationListDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 投递ID
     */
    private Long id;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 职位名称
     */
    private String jobTitle;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 求职者姓名
     */
    private String jobSeekerName;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 状态：0-已投递 1-已查看 2-待面试 3-已面试 4-已录用 5-已拒绝 6-已撤回
     */
    private Integer status;

    /**
     * 匹配度分数（0-100）
     */
    private BigDecimal matchScore;

    /**
     * 匹配分析（JSON格式）
     */
    private String matchAnalysis;

    /**
     * 投递时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

