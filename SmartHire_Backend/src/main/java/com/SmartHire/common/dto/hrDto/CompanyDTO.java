package com.SmartHire.common.dto.hrDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/** 公司信息DTO */
@Data
public class CompanyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 公司ID */
    private Long id;

    /** 公司名称 */
    private String companyName;

    /** 公司简介 */
    private String description;

    /** 公司规模 */
    private Integer companyScale;

    /** 融资阶段 */
    private Integer financingStage;

    /** 公司行业 */
    private String industry;

    /** 公司网站 */
    private String website;

    /** 公司Logo */
    private String logoUrl;

    /** 主要业务 */
    private String mainBusiness;

    /** 福利待遇 */
    private String benefits;

    /** 状态：0-未认证 1-已认证 */
    private Integer status;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 公司成立时间 */
    private Date companyCreatedAt;

    /** 注册资本 */
    private String registeredCapital;
}
