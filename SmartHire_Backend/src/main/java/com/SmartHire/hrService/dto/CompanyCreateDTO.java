package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class CompanyCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 公司名称 */
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    /** 公司简介 */
    @NotBlank(message = "公司简介不能为空")
    private String description;

    /** 公司规模 */
    @NotBlank(message = "公司规模不能为空")
    private Integer companyScale;

    /** 融资阶段 */
    @NotBlank(message = "融资阶段不能为空")
    private Integer financingStage = 0;

    /** 公司行业 */
    @NotBlank(message = "公司行业不能为空")
    private String industry;

    /** 公司网站 */
    private String website;

    /** 公司Logo */
    private String logoUrl;

    /** 福利待遇 */
    private String benefits;

    /** 公司成立时间 */
    @NotBlank(message = "公司成立时间不能为空")
    private Date companyCreatedAt;

    /** 注册资本 */
    @NotBlank(message = "注册资本不能为空")
    private String registeredCapital;
}
