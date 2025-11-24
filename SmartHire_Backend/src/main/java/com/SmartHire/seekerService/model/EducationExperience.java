package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 教育经历表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("education_experience")
public class EducationExperience implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    @JsonIgnore
    private Long jobSeekerId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 专业
     */
    private String major;

    /**
     * 学历
     */
    private Integer education;

    /**
     * 开始日期
     */
    private LocalDate startYear;

    /**
     * 结束日期
     */
    private LocalDate endYear;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonIgnore
    private Date updatedAt;
}
