package com.SmartHire.seekerService.dto.seekerTableDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ProjectExperienceDTO {
  public static final String PRESENT = "至今";
  private static final String MONTH_BODY_REGEX = "(19\\d{2}|20\\d{2})-(0[1-9]|1[0-2])";
  private static final String YEAR_MONTH_REGEX = "^" + MONTH_BODY_REGEX + "$";
  private static final String YEAR_MONTH_OR_PRESENT_REGEX =
      "^((" + MONTH_BODY_REGEX + ")|(" + PRESENT + "))$";

  private Long id;

  public interface Create extends Default {}

  public interface Update extends Default {}

  @NotBlank(message = "项目名称不能为空", groups = Create.class)
  @Size(max = 30, message = "项目名称不能超过30个字符")
  private String projectName;

  @NotBlank(message = "项目角色不能为空", groups = Create.class)
  @Size(max = 30, message = "项目角色不能超过30个字符")
  private String projectRole;

  @NotBlank(message = "项目描述不能为空", groups = Create.class)
  @Size(max = 1000, message = "项目描述不能超过1000个字符")
  private String description;

  @NotBlank(message = "职责描述不能为空", groups = Create.class)
  @Size(max = 1000, message = "职责描述不能超过1000个字符")
  private String responsibility;

  @NotBlank(message = "项目开始时间不能为空", groups = Create.class)
  @Pattern(regexp = YEAR_MONTH_REGEX, message = "项目开始时间格式必须为yyyy-MM")
  private String startMonth;

  @Pattern(regexp = YEAR_MONTH_OR_PRESENT_REGEX, message = "项目结束时间格式必须为yyyy-MM或'至今'")
  private String endMonth;

  @Size(max = 500, message = "项目成果不能超过500个字符")
  private String achievement;

  @Size(max = 255, message = "项目链接不能超过255个字符")
  @URL(message = "项目链接格式不正确")
  private String projectUrl;
}
