package com.SmartHire.seekerService.dto.seekerTableDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;

@Data
public class ResumeDTO {

  public interface Create extends Default {}

  public interface Update extends Default {}

  @NotBlank(message = "简历名称不能为空", groups = Create.class)
  @Size(
      max = 100,
      message = "简历名称长度不能超过100个字符",
      groups = {Create.class, Update.class})
  private String resumeName;

  @Min(
      value = 1,
      message = "隐私级别仅支持1或2",
      groups = {Create.class, Update.class})
  @Max(
      value = 2,
      message = "隐私级别仅支持1或2",
      groups = {Create.class, Update.class})
  private Byte privacyLevel;
}
