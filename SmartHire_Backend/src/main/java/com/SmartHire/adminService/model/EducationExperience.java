package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 教育经历表（在线简历信息）
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("education_experience")
public class EducationExperience implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
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
     * 学历 0-高中及以下 1-专科 2-本科 3-硕士 4-博士
     */
    private Byte education;

    /**
     * 开始年份
     */
    private Date startYear;

    /**
     * 结束年份
     */
    private Date endYear;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
