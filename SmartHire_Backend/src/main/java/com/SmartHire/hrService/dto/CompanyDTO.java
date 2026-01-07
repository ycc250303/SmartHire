package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class CompanyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public interface Create extends Default {}

    public interface Update extends Default {}


    @NotBlank(message = "公司名称不能为空", groups = Create.class)
    /* 公司名称 */
    private String companyName;


    @NotBlank(message = "公司简介不能为空", groups = Create.class)
    /* 公司简介 */
    private String description;

    /** 公司规模 */
    @NotNull(message = "公司规模不能为空", groups = Create.class)
    private Integer companyScale;

    /** 融资阶段 */
    @NotNull(message = "融资阶段不能为空", groups = Create.class)
    private Integer financingStage = 0;

    /** 公司行业 */
    @NotBlank(message = "公司行业不能为空", groups = Create.class)
    private String industry;

    /** 公司网站 */
    private String website;

    /** 公司Logo */
    private String logoUrl;

    /** 福利待遇 */
    private String benefits;

    /** 公司成立时间 */
    @NotNull(message = "公司成立时间不能为空", groups = Create.class)
    private Date companyCreatedAt;

    /** 注册资本 */
    @NotBlank(message = "注册资本不能为空", groups = Create.class)
    private Integer registeredCapital;
}
