package com.SmartHire.seekerService.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 注册/更新求职者用户DTO
 *
 * @author SmartHire Team
 */
@Data
public class SeekerDTO {

    public interface Create extends Default {
    }

    public interface Update extends Default {
    }

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空", groups = Create.class)
    private String realName;

    /**
     * 出生日期
     */
    @NotNull(message = "出生日期不能为空", groups = Create.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    /**
     * 现居住城市
     */
    @NotBlank(message = "现居住城市不能为空", groups = Create.class)
    private String currentCity;

    /**
     * 求职状态
     */
    @NotNull(message = "求职状态不能为空", groups = Create.class)
    @Min(value = 0, message = "求职状态不能小于0", groups = { Create.class, Update.class })
    @Max(value = 3, message = "求职状态不能大于3", groups = { Create.class, Update.class })
    private Integer jobStatus;
}
