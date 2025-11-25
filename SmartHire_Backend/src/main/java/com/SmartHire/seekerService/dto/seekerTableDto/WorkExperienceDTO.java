package com.SmartHire.seekerService.dto.seekerTableDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class WorkExperienceDTO {
    public static final String PRESENT = "至今";
    private static final String MONTH_BODY_REGEX = "(19\\d{2}|20\\d{2})-(0[1-9]|1[0-2])";
    private static final String YEAR_MONTH_REGEX = "^" + MONTH_BODY_REGEX + "$";
    private static final String YEAR_MONTH_OR_PRESENT_REGEX = "^((" + MONTH_BODY_REGEX + ")|(" + PRESENT + "))$";

    private Long id;

    public interface Create extends Default {
    }

    public interface Update extends Default {
    }

    @NotBlank(message = "公司名称不能为空", groups = Create.class)
    @Size(max = 30, message = "公司名称不能超过30个字符")
    private String companyName;

    @NotBlank(message = "职位名称不能为空", groups = Create.class)
    @Size(max = 30, message = "职位名称不能超过30个字符")
    private String position;

    @Size(max = 30, message = "部门名称不能超过30个字符")
    private String department;

    @NotBlank(message = "工作/实习开始时间不能为空", groups = Create.class)
    @Pattern(regexp = YEAR_MONTH_REGEX, message = "工作/实习开始时间格式必须为yyyy-MM")
    private String startMonth;

    @Pattern(regexp = YEAR_MONTH_OR_PRESENT_REGEX, message = "工作/实习结束时间格式必须为yyyy-MM或'至今'")
    private String endMonth;

    @NotBlank(message = "工作/实习描述不能为空", groups = Create.class)
    @Size(max = 1000, message = "工作/实习描述不能超过1000个字符")
    private String description;

    @NotBlank(message = "工作/实习成果不能为空", groups = Create.class)
    @Size(max = 500, message = "工作/实习成果不能超过500个字符")
    private String achievements;

    @NotNull(message = "是否是实习经历不能为空", groups = Create.class)
    private Integer isInternship;
}
