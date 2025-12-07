package com.SmartHire.hrService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 公司信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("company")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 公司规模（tinyint）
     */
    private Integer companyScale;

    /**
     * 融资阶段（tinyint）
     */
    private Integer financingStage;

    /**
     * 公司网站
     */
    private String website;

    /**
     * 公司Logo
     */
    private String logoUrl;

    /**
     * 公司简介
     */
    private String description;

    /**
     * 主要业务
     */
    private String mainBusiness;

    /**
     * 福利待遇
     */
    private String benefits;

    /**
     * 状态：0-未认证 1-已认证
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 公司成立时间
     */
    private Date companyCreatedAt;

    /**
     * 注册资本
     */
    private String registeredCapital;
}

