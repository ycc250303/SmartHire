package com.SmartHire.seekerService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 求职者信息表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-11-19
 */
@Getter
@Setter
@TableName("job_seeker")
public class JobSeeker implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 求职者ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonIgnore
    private Long id;

    /**
     * 用户ID
     */
    @JsonIgnore
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 当前城市
     */
    private String currentCity;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 最高学历
     */
    private Integer education;

    /**
     * 求职状态
     */
    private Integer jobStatus;

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
