package com.SmartHire.seekerService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 注册求职者用户DTO
 *
 * @author SmartHire Team
 */
@Data
public class RegisterSeekerDTO {

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /**
     * 出生日期
     */
    @NotNull(message = "出生日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    /**
     * 现居住城市
     */
    @NotBlank(message = "现居住城市不能为空")
    private String currentCity;

    /**
     * 工作年限
     */
    @NotNull(message = "工作年限不能为空")
    @Min(value = 0, message = "工作年限格式不正确")
    private int workYears;

    /**
     * 最高学历
     */
    @NotNull(message = "最高学历不能为空")
    private int education;
}
