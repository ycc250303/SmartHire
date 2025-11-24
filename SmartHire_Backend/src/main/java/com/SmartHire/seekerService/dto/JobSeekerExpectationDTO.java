package com.SmartHire.seekerService.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobSeekerExpectationDTO {
    public interface Create extends Default {
    }

    public interface Update extends Default {
    }

    @NotBlank(message = "期望职位不能为空", groups = Create.class)
    @Size(max = 100, message = "期望职位长度不能超过100个字符")
    private String expectedPosition;

    @Size(max = 100, message = "期望行业长度不能超过100个字符")
    private String expectedIndustry;

    @NotBlank(message = "期望工作城市不能为空", groups = Create.class)
    @Size(max = 50, message = "期望工作城市长度不能超过50个字符")
    private String workCity;

    @DecimalMin(value = "1000.00", message = "期望薪资最低不能小于1000", groups = Create.class)
    @DecimalMax(value = "100000.00", message = "期望薪资最高不能超过100000（100k）", groups = { Create.class, Update.class })
    @Digits(integer = 6, fraction = 2, message = "期望薪资最低格式不正确", groups = { Create.class, Update.class })
    private BigDecimal salaryMin;

    @DecimalMin(value = "1000.00", message = "期望薪资最高不能小于1000", groups = Create.class)
    @DecimalMax(value = "100000.00", message = "期望薪资最高不能超过100000（100k）", groups = { Create.class, Update.class })
    @Digits(integer = 6, fraction = 2, message = "期望薪资最高格式不正确", groups = { Create.class, Update.class })
    private BigDecimal salaryMax;
}
