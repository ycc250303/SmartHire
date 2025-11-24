package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 求职者期望表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("job_seeker_expectation")
public class JobSeekerExpectation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 求职期望ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 求职者ID
     */
    private Long jobSeekerId;

    /**
     * 期望职位
     */
    private String expectedPosition;

    /**
     * 期望行业
     */
    private String expectedIndustry;

    /**
     * 期望工作城市
     */
    private String workCity;

    /**
     * 期望薪资最低
     */
    private BigDecimal salaryMin;

    /**
     * 期望薪资最高
     */
    private BigDecimal salaryMax;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
