package com.SmartHire.common.dto.hrDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** 求职者端岗位筛选请求 DTO */
@Data
public class JobSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 城市 */
    private String city;

    /** 职位类型：0-全职 1-实习 */
    private Integer jobType;

    /** 学历要求：0-不限 1-专科 2-本科 3-硕士 4-博士 */
    private Integer educationRequired;

    /** 最低薪资筛选（对比岗位最高薪资） */
    @Min(value = 0, message = "最低薪资不能小于0")
    private BigDecimal minSalary;

    /** 最高薪资筛选（对比岗位最低薪资） */
    @Min(value = 0, message = "最高薪资不能小于0")
    private BigDecimal maxSalary;

    /** 关键词（职位名称/描述模糊） */
    private String keyword;

    /** 技能关键字，任意匹配即可 */
    private List<String> skills;

    /** 公司ID，可选 */
    private Long companyId;

    /** 分页页码，默认1 */
    @Positive(message = "页码必须为正整数")
    private Integer page = 1;

    /** 分页大小，默认20，最大100 */
    @Positive(message = "分页大小必须为正整数")
    @Max(value = 100, message = "分页大小不能超过100")
    private Integer size = 20;
}
