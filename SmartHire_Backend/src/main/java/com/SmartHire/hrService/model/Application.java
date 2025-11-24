package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 投递记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("application")
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投递ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

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
     * HR查看时间
     */
    private Date hrViewedAt;

    /**
     * HR评价
     */
    private String hrComment;

    /**
     * 投递时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

