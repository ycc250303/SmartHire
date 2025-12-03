package com.SmartHire.adminService.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 公司信息表
 * </p>
 *
 * @author SmartHire Team
 * @since 2025-12-02
 */
@Getter
@Setter
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
     * 统一社会信用代码
     */
    private String companyCode;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 公司规模
     */
    private String companyScale;

    /**
     * 融资阶段
     */
    private String financingStage;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

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
    private Byte status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
