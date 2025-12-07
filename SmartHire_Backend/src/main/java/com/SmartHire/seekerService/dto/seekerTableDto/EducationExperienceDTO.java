package com.SmartHire.seekerService.dto.seekerTableDto;

import com.SmartHire.seekerService.utils.ValidEducationYears;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import java.time.Year;
import lombok.Data;

@Data
@ValidEducationYears
public class EducationExperienceDTO {
  public interface Create extends Default {}

  public interface Update extends Default {}

  public class YearConstants {
    public static final int CURRENT_YEAR = Year.now().getValue();
  }

  @NotBlank(message = "学校名称不能为空", groups = Create.class)
  private String schoolName;

  @NotBlank(message = "专业不能为空", groups = Create.class)
  private String major;

  @NotNull(groups = Create.class)
  @Min(value = 0, message = "学历格式不正确", groups = Create.class)
  @Max(value = 4, message = "学历格式不正确", groups = Create.class)
  private Integer education;

  @Pattern(regexp = "^\\d{4}$", message = "年份必须是四位数字")
  private String startYear;

  @Pattern(regexp = "^\\d{4}$", message = "年份必须是四位数字")
  private String endYear;
}
