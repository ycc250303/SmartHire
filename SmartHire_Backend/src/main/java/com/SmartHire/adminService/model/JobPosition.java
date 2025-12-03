package com.SmartHire.adminService.model;

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
 * 职位表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("job_position")
public class JobPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职位ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 发布HR的ID
     */
    private Long hrId;

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
     * 详细地址
     */
    private String address;

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
     * 学历要求 0-不限 1-专科 2-本科 3-硕士 4-博士
     */
    private Byte educationRequired;

    /**
     * 工作类型 0-全职 1-兼职 2-实习
     */
    private Byte jobType;

    /**
     * 职位描述
     */
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
     * 状态：0-已下线 1-招聘中 2-已暂停
     */
    private Byte status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 申请次数
     */
    private Integer applicationCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 发布时间
     */
    private Date publishedAt;
}
