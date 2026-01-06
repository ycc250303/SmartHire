package com.SmartHire.hrService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * HR信息创建DTO
 */
@Data
public class HrInfoCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公司ID
     */
    @NotNull(message = "公司ID不能为空")
    private Long companyId;

    /**
     * 真实姓名
     */
    @NotNull(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 职位
     */
    @Size(max = 50, message = "职位长度不能超过50个字符")
    private String position;

    /**
     * 工作电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$|^\\d{3}-\\d{4}-\\d{4}$|^\\d{4}-\\d{7,8}$", message = "工作电话格式不正确")
    @Size(max = 20, message = "工作电话长度不能超过20个字符")
    private String workPhone;

    /** HR类型：0-普通HR，1-公司老板 */
    @NotNull(message = "HR类型不能为空")
    @Min(value = 0, message = "HR类型值不正确")
    @Max(value = 1, message = "HR类型值不正确")
    private Integer hrType;
}
